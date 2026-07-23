package com.billage.domain.post.service;

import com.billage.domain.post.dto.PostCreateRequest;
import com.billage.domain.post.dto.PostResponse;
import com.billage.domain.post.dto.PostSummaryResponse;
import com.billage.domain.post.entity.Post;
import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostType;
import com.billage.domain.post.repository.PostRepository;
import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponse createPost(Long writerId, PostCreateRequest request) {
        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateTypeSpecificFields(request);

        Integer capacity = request.type() == PostType.RENTAL ? 1 : request.capacity();

        Post post = Post.create(
                writer,
                request.category(),
                request.type(),
                request.direction(),
                request.title(),
                request.content(),
                request.location(),
                request.price(),
                capacity,
                request.deadline(),
                request.imageUrl()
        );

        return PostResponse.from(postRepository.save(post));
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    public Page<PostSummaryResponse> getPosts(PostType type, PostCategory category, Pageable pageable) {
        return postRepository.findAllByFilter(type, category, pageable)
                .map(PostSummaryResponse::from);
    }

    public Page<PostSummaryResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable)
                .map(PostSummaryResponse::from);
    }

    /** GROUP_BUY 는 deadline 이 필수입니다. (DB 는 NULL 허용, 여기서만 검증) */
    private void validateTypeSpecificFields(PostCreateRequest request) {
        if (request.type() == PostType.GROUP_BUY && request.deadline() == null) {
            throw new BusinessException(ErrorCode.DEADLINE_REQUIRED);
        }
    }
}
