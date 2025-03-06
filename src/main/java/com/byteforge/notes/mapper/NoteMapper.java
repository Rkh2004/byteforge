package com.byteforge.notes.mapper;

import com.byteforge.notes.model.Note;
import com.byteforge.notes.dto.NoteDTO;

public class NoteMapper {

    public static NoteDTO toDTO(Note note) {
        return new NoteDTO( note.getTitle(), note.getContent());
    }

    public static Note toEntity(NoteDTO noteDTO) {
        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        return note;
    }
}
