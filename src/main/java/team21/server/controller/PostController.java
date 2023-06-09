package team21.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team21.server.auth.details.UserDetailsImpl;
import team21.server.domain.Comment;
import team21.server.domain.Like;
import team21.server.domain.Post;
import team21.server.dto.CommentDto;
import team21.server.dto.PostDto;
import team21.server.mapper.PostMapper;
import team21.server.service.CommentService;
import team21.server.service.LikeService;
import team21.server.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostMapper postMapper;
    private final CommentService commentService;
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity uploadPost(@RequestParam MultipartFile file,
                                     @AuthenticationPrincipal UserDetailsImpl principal) throws IOException {
        long userId = principal.getUserId();
        Post post = postService.createPost(userId, file);
        return new ResponseEntity<>(post.getPostId(), HttpStatus.CREATED);
    }

    @GetMapping("/popularity")
    public ResponseEntity getPostsByPopularity() {
        List<Post> posts = postService.findAllByPopularityDesc();
        List<PostDto> postDtos = postMapper.entitiesToPostDtos(posts);

        return new ResponseEntity<>(postDtos, HttpStatus.OK);
    }

    @GetMapping("/newest")
    public ResponseEntity getPostsByNewest() {
        List<Post> posts = postService.findAllByCreatedAtDesc();
        List<PostDto> postDtos = postMapper.entitiesToPostDtos(posts);

        return new ResponseEntity<>(postDtos, HttpStatus.OK);
    }

    @GetMapping("/{post-id}")
    public ResponseEntity getPostOne(@PathVariable("post-id") long postId) {
        Post post = postService.findPostById(postId);
        PostDto postDto = postMapper.entityToPostDto(post);

        List<Comment> comments = commentService.findCommentsWithPost(postId);

        //익명 설정 시 닉네임 변경
        for(Comment comment : comments) {
            List<Like> likes = likeService.findLikesWithComment(comment.getId());
            String nickName = comment.getUser().getNickName();
            if(comment.getAnonymous().equals(true)) {
                nickName = "익명";
            }

            CommentDto commentDto = new CommentDto(comment.getAnonymous(),
                                                    postId,
                                                    nickName,
                                                    comment.getUser().getImageName(),
                                                    comment.getBody(),
                                                    (long) likes.size());

            postDto.getComments().add(commentDto);
        }

        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }

    @DeleteMapping("/{post-id}")
    public ResponseEntity deletePost(@PathVariable("post-id") long postId,
                                     @AuthenticationPrincipal UserDetailsImpl principal) {
        long userId = principal.getUserId();
        postService.deletePost(userId, postId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
