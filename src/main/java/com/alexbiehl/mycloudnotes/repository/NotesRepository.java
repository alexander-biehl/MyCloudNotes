package com.alexbiehl.mycloudnotes.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.alexbiehl.mycloudnotes.dto.Note;

public interface NotesRepository extends CrudRepository<Note, UUID> {

}
