package com.clouddrive.filesservice.dto;

import com.clouddrive.filesservice.model.Datoteka;
import com.clouddrive.filesservice.model.Folder;
import java.util.List;

public class SadrzajFolderaDto {
    private List<Folder> folderi;
    private List<Datoteka> datoteke;

    public SadrzajFolderaDto(List<Folder> folderi, List<Datoteka> datoteke) {
        this.folderi = folderi;
        this.datoteke = datoteke;
    }

    public List<Folder> getFolderi() { return folderi; }
    public List<Datoteka> getDatoteke() { return datoteke; }
}