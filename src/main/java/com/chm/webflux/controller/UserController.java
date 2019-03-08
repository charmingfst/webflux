package com.chm.webflux.controller;

import com.chm.webflux.domain.User;
import com.chm.webflux.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserRepository userRepository;

    // 使用构造方法注入
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public Flux<User> getAll() {
        return userRepository.findAll();
    }

    // 以SSE(server-send event)形式返回数据
    @GetMapping(value = "/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> getAllStream() {
        return userRepository.findAll();
    }

    @PostMapping("/add")
    public Mono<User> createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    /**
     * 存在就删除，成功返回200， 不存在返回404
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") String id) {
        // deleteById没有返回值，不能判断数据是否存在
        userRepository.findById(id).map(user -> userRepository.delete(user).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(Mono.just(new ResponseEntity(HttpStatus.NOT_FOUND)));

        return userRepository.findById(id).flatMap(user ->  userRepository.delete(user).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    /**
     * 存在的时候返回200和更新后的数据
     * 不存在返回404
     * @param id
     * @return
     */
    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<User>> updateUser(@PathVariable("id") String id, @RequestBody User user) {
        return userRepository.findById(id).flatMap(u -> {u.setName(user.getName()); u.setAge(user.getAge());return userRepository.save(u);}).map(u -> new ResponseEntity<User>(u, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
