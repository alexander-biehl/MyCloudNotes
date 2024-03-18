package com.alexbiehl.mycloudnotes.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alexbiehl.mycloudnotes.dto.Note;

public interface NotesRepository extends JpaRepository<Note, UUID> {

}
