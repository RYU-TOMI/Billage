package com.billage.domain.post.repository;

import com.billage.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 목록 조회용 type/category 필터 쿼리는 게시글 트랙에서 추가 예정
}
