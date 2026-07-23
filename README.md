# Billage

해커톤 프로젝트 **Billage** 백엔드 저장소입니다.

## 소개

Billage는 대학생을 위한 생활 공유 플랫폼입니다.

학교 인증을 기반으로 같은 학교 학생들끼리 생활용품과 공구를 안전하게 대여하고, 휴지·세제 등 생필품은 공동구매하여 생활비를 절약할 수 있도록 돕습니다.

## 기술 스택

확정되는 대로 추가 예정입니다.

## 주요 기능

정리되는 대로 추가 예정입니다.

## API 명세서

작성되는 대로 추가 예정입니다.

## 브랜치 전략

기능 단위로 브랜치를 나누는 feature-driven 방식으로 진행합니다.

- `main` — 배포 가능한 안정 브랜치
- `feature/*` — 기능 단위 작업 브랜치 (예: `feature/login`, `feature/rental`)

기능마다 `main`에서 `feature/*` 브랜치를 따서 작업하고, 완료되면 `main`으로 PR을 올려 리뷰 후 머지합니다.

## 커밋 컨벤션

```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 코드 리팩토링
docs: 문서 수정
chore: 빌드, 설정 등 기타 변경
```

## 시작하기

```bash
git clone https://github.com/RYU-TOMI/Billage.git
cd Billage
```
