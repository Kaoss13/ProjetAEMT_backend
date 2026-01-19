package com.helha.projetaemt_backend.application.folder.command.create;

import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class CreateFolerHandler implements ICommandHandler<CreateFolderInput, CreateFolderOutput> {
    private final IFolderRepository folderRepository;
    private final ModelMapper modelMapper;

    public CreateFolerHandler(IFolderRepository folderRepository, ModelMapper modelMapper){
        this.folderRepository = folderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CreateFolderOutput handle(CreateFolderInput input){

    }
}
