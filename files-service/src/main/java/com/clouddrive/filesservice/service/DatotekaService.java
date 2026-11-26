package com.clouddrive.filesservice.service;

import com.clouddrive.filesservice.dto.PromenaNazivaDto;
import com.clouddrive.filesservice.model.Datoteka;
import com.clouddrive.filesservice.model.Folder;
import com.clouddrive.filesservice.repository.DatotekaRepository;
import com.clouddrive.filesservice.repository.FolderRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class DatotekaService {

    private static final long MAX_UPLOAD_BYTES = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of(".pdf", ".png", ".jpg", ".jpeg", ".txt");
    private final DatotekaRepository datotekaRepository;
    private final FolderRepository folderRepository;
    private final GridFsTemplate gridFsTemplate;
    private final RestClient usersClient;
    private final String interniToken;

    public DatotekaService(
            DatotekaRepository datotekaRepository,
            FolderRepository folderRepository,
            GridFsTemplate gridFsTemplate,
            RestClient.Builder restClientBuilder,
            @Value("${users.service.url}") String usersServiceUrl,
            @Value("${internal.service.token:}") String interniToken) {
        this.datotekaRepository = datotekaRepository;
        this.folderRepository = folderRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.usersClient = restClientBuilder.baseUrl(usersServiceUrl).build();
        this.interniToken = interniToken;
    }

    public List<Datoteka> datotekeUFolderu(String vlasnikId, String folderId) {
        if (folderId == null || folderId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return datotekaRepository.findByVlasnikIdAndFolderId(vlasnikId, folderId);
    }

    public Datoteka otpremi(String vlasnikId, String folderId, MultipartFile fajl) {
        proveriFolder(vlasnikId, folderId);
        String naziv = validirajFajl(fajl);
        if (datotekaRepository.existsByVlasnikIdAndFolderIdAndNaziv(vlasnikId, folderId, naziv)) {
            throw new IllegalArgumentException("Datoteka sa ovim nazivom već postoji. Izaberite novu verziju.");
        }
        long velicina = fajl.getSize();
        promeniZauzece(vlasnikId, velicina);
        ObjectId noviGridFsId = null;
        try {
            noviGridFsId = sacuvajSadrzaj(fajl, naziv);
            Datoteka datoteka = new Datoteka();
            datoteka.setNaziv(naziv);
            datoteka.setTip(tip(fajl));
            datoteka.setVelicina(velicina);
            datoteka.setVlasnikId(vlasnikId);
            datoteka.setFolderId(folderId);
            datoteka.setGridFsId(noviGridFsId.toHexString());
            datoteka.setSha256(sha256(fajl));
            datoteka.setVremeIzmene(LocalDateTime.now());
            return datotekaRepository.save(datoteka);
        } catch (RuntimeException | IOException e) {
            obrisiSadrzajBezGreske(noviGridFsId);
            vratiZauzeceIliPrijavi(vlasnikId, velicina, e);
            throw new IllegalStateException("Otpremanje nije uspelo: " + e.getMessage(), e);
        }
    }

    public Datoteka novaVerzija(String id, String vlasnikId, MultipartFile fajl) {
        Datoteka datoteka = datotekaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Datoteka ne postoji."));
        if (vlasnikId == null || !vlasnikId.equals(datoteka.getVlasnikId())) {
            throw new IllegalArgumentException("Nemate pristup ovoj datoteci.");
        }
        String naziv = validirajFajl(fajl);
        long staraVelicina = datoteka.getVelicina() == null ? 0L : datoteka.getVelicina();
        long promena = fajl.getSize() - staraVelicina;
        promeniZauzece(vlasnikId, promena);
        ObjectId noviGridFsId = null;
        try {
            noviGridFsId = sacuvajSadrzaj(fajl, naziv);
            String stariGridFsId = datoteka.getGridFsId();
            datoteka.setTip(tip(fajl));
            datoteka.setVelicina(fajl.getSize());
            datoteka.setGridFsId(noviGridFsId.toHexString());
            datoteka.setSha256(sha256(fajl));
            datoteka.setVremeIzmene(LocalDateTime.now());
            Datoteka sacuvana = datotekaRepository.save(datoteka);

            if (stariGridFsId != null && ObjectId.isValid(stariGridFsId)) {
                obrisiSadrzajBezGreske(new ObjectId(stariGridFsId));
            }
            return sacuvana;
        } catch (RuntimeException | IOException e) {
            obrisiSadrzajBezGreske(noviGridFsId);
            vratiZauzeceIliPrijavi(vlasnikId, promena, e);
            throw new IllegalStateException("Nova verzija nije sačuvana: " + e.getMessage(), e);
        }
    }

    public Datoteka preimenuj(String id, PromenaNazivaDto dto) {
        String naziv = dto.getNaziv().trim();
        proveriNaziv(naziv);
        String vlasnikId = dto.getVlasnikId().trim();
        Datoteka datoteka = datotekaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Datoteka nije pronađena!"));
        if (!vlasnikId.equals(datoteka.getVlasnikId())) {
            throw new IllegalArgumentException("Nemate pristup ovoj datoteci!");
        }
        if (datotekaRepository.existsByVlasnikIdAndFolderIdAndNazivAndIdNot(
                vlasnikId, datoteka.getFolderId(), naziv, id)) {
            throw new IllegalArgumentException("Datoteka sa tim nazivom već postoji u folderu!");
        }
        datoteka.setNaziv(naziv);
        datoteka.setVremeIzmene(LocalDateTime.now());
        return datotekaRepository.save(datoteka);
    }

    private Folder proveriFolder(String vlasnikId, String folderId) {
        if (vlasnikId == null || vlasnikId.isBlank() || folderId == null || folderId.isBlank()) {
            throw new IllegalArgumentException("Izaberite folder i prijavljenog korisnika.");
        }
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder ne postoji."));
        if (!vlasnikId.equals(folder.getVlasnikId())) {
            throw new IllegalArgumentException("Nemate pravo pristupa folderu.");
        }
        return folder;
    }

    private String validirajFajl(MultipartFile fajl) {
        if (fajl == null || fajl.isEmpty()) {
            throw new IllegalArgumentException("Izaberite neprazan fajl.");
        }
        String naziv = fajl.getOriginalFilename();
        if (naziv == null) {
            throw new IllegalArgumentException("Fajl nema naziv.");
        }
        proveriNaziv(naziv);
        if (fajl.getSize()>MAX_UPLOAD_BYTES) throw new IllegalArgumentException("Fajl je veci od 50 MB.");
        String normalizovano = naziv.toLowerCase(Locale.ROOT);
        int dot = normalizovano.lastIndexOf('.');
        if (dot < 0) throw new IllegalArgumentException("Fajl mora imati ekstenziju.");
        String extension = normalizovano.substring(dot);
        if (!ALLOWED.contains(extension))
            throw new IllegalArgumentException("Dozvoljeni su PDF, PNG, JPG/JPEG i TXT fajlovi.");
        try {
            byte[] head;
            try (InputStream in = fajl.getInputStream()) { head = in.readNBytes(8); }
            if (extension.equals(".pdf") && !startsWith(head, new byte[]{0x25,0x50,0x44,0x46,0x2d}))
                throw new IllegalArgumentException("Neispravan PDF fajl.");
            if (extension.equals(".png") && !startsWith(head, new byte[]{(byte)137,80,78,71,13,10,26,10}))
                throw new IllegalArgumentException("Neispravna PNG slika.");
            if ((extension.equals(".jpg") || extension.equals(".jpeg")) &&
                !startsWith(head, new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff}))
                throw new IllegalArgumentException("Neispravna JPEG slika.");
            if (extension.equals(".txt")) {
                StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(fajl.getBytes()));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Neispravan sadrzaj fajla.", e);
        }
        return naziv;
    }

    private boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length<prefix.length) return false;
        for (int i=0;i<prefix.length;i++) if (bytes[i]!=prefix[i]) return false;
        return true;
    }

    private void proveriNaziv(String naziv) {
        if (naziv == null || naziv.isBlank()) {
            throw new IllegalArgumentException("Naziv ne sme biti prazan!");
        }
        if (naziv.chars().anyMatch(ch -> Character.isISOControl(ch) || ch == '<' || ch == '>') ||
            naziv.contains("/") || naziv.contains("\\") || naziv.contains("..") || naziv.length() > 255) {
            throw new IllegalArgumentException("Naziv fajla nije dozvoljen (maks. 255 karaktera). ");
        }
    }

    private String tip(MultipartFile fajl) {
        String naziv = fajl.getOriginalFilename();
        if (naziv == null) return "application/octet-stream";
        String lower = naziv.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    private ObjectId sacuvajSadrzaj(MultipartFile fajl, String naziv) throws IOException {
        return gridFsTemplate.store(fajl.getInputStream(), naziv, tip(fajl));
    }

    private void obrisiSadrzajBezGreske(ObjectId id) {
        if (id == null) return;
        try {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(id)));
        } catch (RuntimeException e) {
            System.err.println("GridFS čišćenje nije uspelo za ID " + id + ": " + e.getMessage());
        }
    }

    private String sha256(MultipartFile fajl) throws IOException {
        try (InputStream in = fajl.getInputStream()) { return digest(in); }
    }

    private String digest(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] block = new byte[8192];
            int count;
            while ((count=in.read(block))!=-1) md.update(block,0,count);
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public boolean proveriIntegritet(String id, String owner) {
        Datoteka file = datotekaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Datoteka nije pronadjena"));
        if (!owner.equals(file.getVlasnikId())) throw new IllegalArgumentException("Nije dozvoljen pristup");
        if (file.getSha256() == null || file.getGridFsId() == null ||
            !ObjectId.isValid(file.getGridFsId()))
            throw new IllegalStateException("Kontrolni zbir ne postoji za ovu datoteku");
        GridFSFile stored = gridFsTemplate.findOne(Query.query(
                Criteria.where("_id").is(new ObjectId(file.getGridFsId()))));
        if (stored == null) return false;
        try (InputStream in = gridFsTemplate.getResource(stored).getInputStream()) {
            return file.getSha256().equals(digest(in));
        } catch (IOException e) { throw new IllegalStateException("Provera integriteta nije uspela",e); }
    }

    private void promeniZauzece(String vlasnikId, long promena) {
        if (promena == 0) return;
        if (interniToken == null || interniToken.isBlank()) {
            throw new IllegalStateException("INTERNAL_SERVICE_TOKEN nije podešen.");
        }
        try {
            usersClient.post()
                    .uri(uri -> uri.path("/api/internal/kvota/{id}/promeni")
                            .queryParam("promena", promena).build(vlasnikId))
                    .header("X-Internal-Token", interniToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new IllegalStateException("Provera kvote nije uspela ili nema dovoljno prostora: " + e.getMessage(), e);
        }
    }

    private void vratiZauzeceIliPrijavi(String vlasnikId, long ranijaPromena, Exception originalnaGreska) {
        try {
            promeniZauzece(vlasnikId, -ranijaPromena);
        } catch (RuntimeException greskaVracanja) {
            originalnaGreska.addSuppressed(greskaVracanja);
            System.err.println("VAŽNO: Nije uspelo vraćanje kvote korisnika " + vlasnikId);
        }
    }
}
