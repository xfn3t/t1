package ru.homework.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.jwt.dto.request.UserRequestDto;
import ru.homework.jwt.dto.response.UserResponseDto;
import ru.homework.jwt.exception.UserNotFoundException;
import ru.homework.jwt.mapper.UserMapper;
import ru.homework.jwt.model.user.User;
import ru.homework.jwt.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id=" + id + " not found"));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto create(UserRequestDto dto) {
        User entity = userMapper.toEntity(dto);
        User saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserResponseDto update(Long id, UserRequestDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id=" + id + " not found"));
        existing.setUsername(dto.getUsername());
        existing.setEmail(dto.getEmail());
        return userMapper.toDto(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id=" + id + " not found");
        }
        userRepository.deleteById(id);
    }

    public void someHeavyOperation() {
        try { Thread.sleep(600); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
