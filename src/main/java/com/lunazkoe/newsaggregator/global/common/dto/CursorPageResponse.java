package com.lunazkoe.newsaggregator.global.common.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        String nextCursor, // String으로 해서 어떤 cursor든 담을 수 있음
        String nextAfter, // 보조 커서용
        int size,
        long totalElements,
        boolean hasNext
) {
}

/*
```
// 예시: 클라이언트가 받게 될 JSON
{
  "content": [...],
  "nextCursor": "1050", // 마지막 기사 ID
  "nextAfter": "2026-05-11T18:00:00", // 마지막 기사 작성일
  "size": 10,
  "totalElements": 500,
  "hasNext": true
}
```
*/