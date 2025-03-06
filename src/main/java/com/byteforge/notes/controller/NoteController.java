package com.byteforge.notes.controller;

import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.exception.ResourceNotFoundException;
import com.byteforge.notes.dto.NoteDTO;
import com.byteforge.notes.mapper.NoteMapper;
import com.byteforge.notes.model.Note;
import com.byteforge.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    //helper method to authenticate user
    private User getAuthenticatedUser(Principal principal) {
        String username = principal.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<NoteDTO>> getAllNotes(Principal principal) {
        User user = getAuthenticatedUser(principal);
        List<Note> notes = noteService.getAllNotesForUser(user);
        List<NoteDTO> noteDTOs = notes.stream()
                .map(NoteMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(noteDTOs);
    }

    @PostMapping
    public ResponseEntity<NoteDTO> createNote(Principal principal, @RequestBody NoteDTO noteDTO) {
        User user = getAuthenticatedUser(principal);
        Note note = NoteMapper.toEntity(noteDTO);
        Note savedNote = noteService.createNote(note, user);
        return ResponseEntity.ok(NoteMapper.toDTO(savedNote));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        Note note = noteService.getNoteById(id, user);
        return ResponseEntity.ok(NoteMapper.toDTO(note));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<NoteDTO> updateNote(Principal principal, @PathVariable Long id, @RequestBody NoteDTO noteDTO) {
        User user = getAuthenticatedUser(principal);
        Note updatedNote = NoteMapper.toEntity(noteDTO);
        updatedNote.setUser(user);
        Note savedNote = noteService.updateNote(id, updatedNote, user);
        return ResponseEntity.ok(NoteMapper.toDTO(savedNote));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNote(Principal principal, @PathVariable Long id) {
        User user = getAuthenticatedUser(principal);
        noteService.deleteNote(id, user);
        return ResponseEntity.ok("Note deleted successfully");
    }
}
