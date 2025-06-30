package ru.t1.homework.cache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.homework.cache.annotation.Cached;
import ru.t1.homework.cache.annotation.Metric;
import ru.t1.homework.cache.dto.request.UserRequestDto;
import ru.t1.homework.cache.dto.response.UserResponseDto;
import ru.t1.homework.cache.exception.UserNotFoundException;
import ru.t1.homework.cache.mapper.UserMapper;
import ru.t1.homework.cache.model.User;
import ru.t1.homework.cache.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cached
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

    @Metric
    public void someHeavyOperation() {
        try { Thread.sleep(600); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
