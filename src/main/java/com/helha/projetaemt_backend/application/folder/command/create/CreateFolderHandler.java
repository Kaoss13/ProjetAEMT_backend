package com.helha.projetaemt_backend.application.folder.command.create;

import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class CreateFolderHandler implements ICommandHandler<CreateFolderInput, CreateFolderOutput> {
    private final IFolderRepository folderRepository;
    private final ModelMapper modelMapper;

    public CreateFolderHandler(IFolderRepository folderRepository, ModelMapper modelMapper){
        this.folderRepository = folderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CreateFolderOutput handle(CreateFolderInput input){
        //Validation du titre
        if(input.title == null || input.title.trim().isEmpty()){
            throw new IllegalArgumentException("The file title is mandatory.");
        }


        //Mapping via le ModelMapper
        DbFolder entity = modelMapper.map(input, DbFolder.class);
        DbFolder saveEntity = folderRepository.save(entity);
        return modelMapper.map(saveEntity, CreateFolderOutput.class);
    }
}
