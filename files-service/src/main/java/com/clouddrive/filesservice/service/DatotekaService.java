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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class DatotekaService {

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
        return naziv;
    }

    private void proveriNaziv(String naziv) {
        if (naziv == null || naziv.isBlank()) {
            throw new IllegalArgumentException("Naziv ne sme biti prazan!");
        }
        if (naziv.contains("/") || naziv.contains("\\") || naziv.contains("..") || naziv.length() > 255) {
            throw new IllegalArgumentException("Naziv fajla nije dozvoljen (maks. 255 karaktera). ");
        }
    }

    private String tip(MultipartFile fajl) {
        String vrednost = fajl.getContentType();
        return vrednost == null || vrednost.isBlank() ? "application/octet-stream" : vrednost;
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
