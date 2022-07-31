package io.github.hejcz.users.signup;

import io.github.hejcz.users.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SignupFormMapper {

    SignupFormMapper INSTANCE = Mappers.getMapper(SignupFormMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    User map(SignupForm form);

}
