package com.sparta_a5.ootd.admin.service;

import com.querydsl.core.BooleanBuilder;
import com.sparta_a5.ootd.admin.dto.SearchRequestDto;
import com.sparta_a5.ootd.common.configuration.JwtUtil;
import com.sparta_a5.ootd.admin.dto.AdminUpdateRequestDto;
import com.sparta_a5.ootd.post.dto.PostResponseDto;
import com.sparta_a5.ootd.post.entity.Post;
import com.sparta_a5.ootd.post.entity.QPost;
import com.sparta_a5.ootd.post.repository.PostQueryRepository;
import com.sparta_a5.ootd.post.repository.PostRepository;
import com.sparta_a5.ootd.user.dto.LoginRequestDto;
import com.sparta_a5.ootd.user.dto.UserResponseDto;
import com.sparta_a5.ootd.user.entity.QUser;
import com.sparta_a5.ootd.user.entity.User;
import com.sparta_a5.ootd.user.entity.UserRoleEnum;
import com.sparta_a5.ootd.user.repository.UserQueryRepository;
import com.sparta_a5.ootd.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sparta_a5.ootd.user.entity.UserRoleEnum.ADMIN;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostQueryRepository postQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final JwtUtil jwtUtil;

    public void adminLogin(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new IllegalArgumentException("등록된 유저가 없습니다.")
        );

        if(passwordEncoder.matches(password,user.getPassword())){
            if(user.getRole()!=ADMIN){
                throw new IllegalArgumentException("권한이 없습니다.");
            }
        }else{
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        jwtUtil.addJwtToCookie(jwtUtil.createToken(user.getUsername(),user.getRole()),response);
    }

    @Transactional
    public UserResponseDto updateUserRole(Long userId, AdminUpdateRequestDto requestDto, User adminUser) {

        if(adminUser.getRole() != ADMIN){
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(
                ()-> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );

        user.setUserRole(UserRoleEnum.valueOf(requestDto.getUserRole()));
        return new UserResponseDto(user);
    }

    @Transactional
    public void deletePost(Long postId, User adminUser) {

        if(adminUser.getRole() != ADMIN){
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );

        postRepository.delete(post);
    }

    @Transactional
    public void deleteUser(Long userId, User adminUser) {

        if(adminUser.getRole() != ADMIN){
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(
                ()-> new IllegalArgumentException("존재하지 않는 사용자입니다.")
        );

        userRepository.delete(user);
    }

    public List<UserResponseDto> getUserList(SearchRequestDto searchRequestDto) {
        BooleanBuilder builder = new BooleanBuilder();

       if(searchRequestDto.getClassification().equals("username")){
            builder.and(QUser.user.username.eq(searchRequestDto.getKeyword()))
                    .and(QUser.user.createdAt.between(searchRequestDto.getStartDate(),searchRequestDto.getEndDate()));
       } else if (searchRequestDto.getClassification().equals("email")){
           builder.and(QUser.user.email.contains(searchRequestDto.getKeyword()))
                   .and(QUser.user.createdAt.between(searchRequestDto.getStartDate(),searchRequestDto.getEndDate()));
       }

        List<User> userList = userQueryRepository.findAll(builder);
        return userList.stream().map(user -> UserResponseDto.builder()
                .user(user)
                .build()
        ).toList();
    }

    public List<PostResponseDto> getPostList(SearchRequestDto searchRequestDto) {
        BooleanBuilder builder = new BooleanBuilder();

        if(searchRequestDto.getClassification().equals("title")){
            builder.and(QPost.post.title.contains(searchRequestDto.getKeyword()))
                    .and(QPost.post.createdAt.between(searchRequestDto.getStartDate(),searchRequestDto.getEndDate()));
        } else if(searchRequestDto.getClassification().equals("content")){
            builder.and(QPost.post.content.contains((searchRequestDto.getKeyword())))
                    .and(QPost.post.createdAt.between(searchRequestDto.getStartDate(),searchRequestDto.getEndDate()));

        } else if(searchRequestDto.getClassification().equals("username")){
            builder.and(QPost.post.user.username.contains(searchRequestDto.getKeyword()))
                    .and(QPost.post.createdAt.between(searchRequestDto.getStartDate(),searchRequestDto.getEndDate()));
        }

        List<Post> postList = postQueryRepository.findAll(builder);
        return postList.stream().map(post -> PostResponseDto.builder()
                .post(post)
                .build()
        ).toList();
    }
}