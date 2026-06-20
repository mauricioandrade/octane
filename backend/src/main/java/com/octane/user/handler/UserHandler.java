package com.octane.user.handler;

import com.octane.user.usecase.CreateUserRequest;
import com.octane.user.usecase.CreateUserUseCase;
import com.octane.user.usecase.FindUserUseCase;
import com.octane.user.usecase.ListUsersUseCase;
import com.octane.user.usecase.UpdateUserRequest;
import com.octane.user.usecase.UpdateUserUseCase;
import com.octane.user.usecase.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserHandler {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final FindUserUseCase findUserUseCase;

    public UserHandler(CreateUserUseCase createUserUseCase,
                       UpdateUserUseCase updateUserUseCase,
                       ListUsersUseCase listUsersUseCase,
                       FindUserUseCase findUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.findUserUseCase = findUserUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return createUserUseCase.execute(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateUserRequest request) {
        return updateUserUseCase.execute(id, request);
    }

    @GetMapping
    public List<UserResponse> list() {
        return listUsersUseCase.execute();
    }

    @GetMapping("/{id}")
    public UserResponse find(@PathVariable UUID id) {
        return findUserUseCase.execute(id);
    }
}
