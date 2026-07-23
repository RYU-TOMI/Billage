package com.billage.domain.post.entity;

/** 글쓴이가 물건을 빌려주는 입장(LEND)인지, 빌리고 싶은 입장(BORROW)인지. 화면 표시/필터링 용도이며 결제 로직에는 영향 없음. */
public enum PostDirection {
    LEND,
    BORROW
}
