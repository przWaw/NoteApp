package pl.wawrzyniak.NoteApp.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.wawrzyniak.NoteApp.Criteria.NoteCriteria;
import pl.wawrzyniak.NoteApp.Repository.CustomExeption.NoteNotExistsException;
import pl.wawrzyniak.NoteApp.Repository.CustomExeption.VerificationException;
import pl.wawrzyniak.NoteApp.Repository.Entities.Note;
import pl.wawrzyniak.NoteApp.Repository.NoteRepository;
import pl.wawrzyniak.NoteApp.Service.DTO.NoteDTO;
import pl.wawrzyniak.NoteApp.Service.DTO.Page;
import pl.wawrzyniak.NoteApp.Service.DTO.PaginationInfo;
import pl.wawrzyniak.NoteApp.Service.Mappers.CriteriaMapper;
import pl.wawrzyniak.NoteApp.Service.Mappers.NoteMapper;
import pl.wawrzyniak.NoteApp.Service.Verifier.NoteVerifier;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private NoteRepository noteRepository;
    private NoteMapper noteMapper;
    private CriteriaMapper criteriaMapper;
    private List<NoteVerifier> noteVerifiers;

    @Autowired
    public NoteService(NoteRepository noteRepository, NoteMapper noteMapper, CriteriaMapper criteriaMapper, List<NoteVerifier> noteVerifier){
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
        this.criteriaMapper = criteriaMapper;
        this.noteVerifiers = noteVerifier;
    }

    public NoteDTO save(NoteDTO note) throws VerificationException {
        for(NoteVerifier verifier : noteVerifiers){
            verifier.verify(note);
        }
        Note noteToSave = this.noteMapper.noteDTOToNote(note);
        Note savedNote =  noteRepository.save(noteToSave);
        return this.noteMapper.noteToNoteDTO(savedNote);
    }

    public void deleteAll() {
        noteRepository.deleteAll();
    }

    public Page getByCriteria(PaginationInfo info){
        info.setDefault();
        NoteCriteria criteria = criteriaMapper.dtoToCriteria(info.getCriteria());
        List<NoteDTO> notes = noteMapper.noteListToDTOList(noteRepository.findByCriteria(criteria, info.getOffset(), info.getPageNumber(), info.getPageSize()));
        PaginationInfo updatedInfo = new PaginationInfo();
        updatedInfo.setOffset(info.getOffset());
        updatedInfo.setPageSize(info.getPageSize());
        updatedInfo.setPageNumber(info.getPageNumber());
        updatedInfo.setCriteria(info.getCriteria());
        updatedInfo.setAllPages(noteRepository.count() / updatedInfo.getPageSize());
        if(noteRepository.count() % updatedInfo.getPageSize() > 0){
            updatedInfo.setAllPages(updatedInfo.getAllPages() + 1);
        }
        Page page = new Page();
        page.setNotes(notes);
        page.setPaginationInfo(updatedInfo);
        return page;
    }

    public NoteDTO update(NoteDTO noteDTO) throws NoteNotExistsException {
        Optional<Note> note = noteRepository.findById(noteDTO.getId());
        if(note.isEmpty()) {
            throw new NoteNotExistsException();
        }
        Note noteToUpdate = this.noteMapper.noteDTOToNote(noteDTO);
        return this.noteMapper.noteToNoteDTO(noteRepository.save(noteToUpdate));
    }

    public void deleteById(Long id) throws NoteNotExistsException {
        Optional<Note> note = this.noteRepository.findById(id);
        if(note.isEmpty()){
            throw new NoteNotExistsException();
        }
        this.noteRepository.deleteById(id);
    }
}
