package com.byteforge.notes.service;

import com.byteforge.notes.model.Note;
import com.byteforge.notes.repository.NoteRepository;
import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Note> getAllNotesForUser(User user) {
        return noteRepository.findByUser(user);
    }

    public Note createNote(Note note, User user) {

        note.setUser(user);
        return noteRepository.save(note);
    }

    public Note getNoteById(Long id, User user) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to note");
        }

        return note;
    }

    public Note updateNote(Long id, Note updatedNote, User user) {
        Note note = getNoteById(id, user); // Fetch existing note

        note.setTitle(updatedNote.getTitle());
        note.setContent(updatedNote.getContent());

        return noteRepository.save(note);
    }

    public void deleteNote(Long id, User user) {
        Note note = getNoteById(id, user);
        noteRepository.delete(note);
    }
}
