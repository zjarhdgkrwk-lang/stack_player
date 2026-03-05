# Stack — Claude Code 프롬프트 가이드

## 사전 준비

Repository 루트에 SSOT 문서를 배치합니다:
```
your-repo/
└── docs/
    └── Stack_SSOT.md
```

## 프롬프트 전략

- **Phase별로 하나의 프롬프트**를 줍니다 (한 번에 전체 X)
- 각 Phase 완료 후 **빌드 성공을 확인**하고 다음 Phase로 넘어갑니다
- Phase가 끝날 때마다 `git commit` 합니다
- 이전 Phase에서 만든 코드를 다음 Phase가 참조하므로 **순서를 반드시 지킵니다**

---

## Phase 1 프롬프트 (Foundation)

```
docs/Stack_SSOT.md 파일을 처음부터 끝까지 읽어라. 이 문서가 이 프로젝트의 SSOT(Single Source of Truth)다. 모든 구현은 이 문서를 따른다.

지금부터 Phase 1 (Foundation)을 구현한다.

## 할 일

1. **프로젝트 생성**: Android 프로젝트를 SSOT Section 1.3의 설정대로 생성
2. **멀티모듈 구조**: SSOT Section 4.1의 13개 모듈을 전부 생성하고 settings.gradle.kts에 include
3. **Gradle 설정**: SSOT Appendix A의 libs.versions.toml, root build.gradle.kts를 그대로 사용
4. **각 모듈의 build.gradle.kts**: SSOT Section 4.2의 의존성 규칙을 엄격히 준수
5. **:core 모듈 구현**:
   - CoroutineDispatchers, Result, DateTimeUtil, DurationUtil, StringUtil, FileUtil, FlowExtensions
   - Logger + LoggerModule (Timber 기반)
   - CrashCapture (인터페이스 + 구현)
   - DispatcherModule (Hilt)
   - Theme, Color, Typography, Spacing, Shape (Material3)
   - UI Components: LoadingState, ErrorState, EmptyState, ArtworkImage
6. **:domain 모듈 구현**:
   - 모든 Domain model (SSOT Section 8.3 그대로)
   - 모든 Enum (SSOT Section 8.3 그대로)
   - 모든 Repository 인터페이스 (SSOT Section 4.3의 domain/repository/ 참고)
   - 모든 UseCase 껍데기 (클래스 생성 + TODO 바디)
7. **BaseViewModel**: SSOT Section 22.1의 MVI 패턴 구현

## 규칙
- SSOT Section 2.2의 의존성 규칙을 절대 위반하지 마라
- 모든 string은 SSOT Section 2.5에 따라 리소스 처리하되, 이 Phase에서는 영어 strings.xml만 생성
- `./gradlew :app:assembleDebug` 빌드가 반드시 성공해야 한다

## 완료 조건
- [ ] 13개 모듈 전부 생성되고 Gradle Sync 성공
- [ ] :core 유틸리티 전부 컴파일 성공
- [ ] :domain 모델/인터페이스 전부 컴파일 성공
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 2 프롬프트 (Data Layer)

```
docs/Stack_SSOT.md를 읽어라. Phase 1이 완료된 상태다.

지금부터 Phase 2 (Data Layer)를 구현한다.

## 할 일

1. **Room Database**: SSOT Section 8.2의 13개 Entity를 그대로 구현
2. **FTS 테이블**: TrackFtsEntity (SSOT Section 8.2 참고)
3. **TypeConverters**: 필요한 타입 변환기 구현
4. **DAO 전부 구현**: SSOT Section 4.4의 dao/ 디렉토리 참고. 각 Entity에 대한 CRUD + 필요한 쿼리 메서드
5. **DataStore**: PreferencesDataStore + PreferencesKeys (SSOT Section 8.4 그대로)
6. **Mapper**: Entity ↔ Domain model 변환기 전부
7. **Repository 구현체**: :domain의 모든 Repository 인터페이스에 대한 Impl 클래스
8. **DI 모듈**: DatabaseModule, RepositoryModule, ScannerModule
9. **StackDatabase**: @Database 어노테이션에 모든 Entity 등록, exportSchema = true

## 규칙
- :data는 :domain과 :core에만 의존한다
- Room schema는 data/schemas/에 export
- DAO 쿼리는 suspend 또는 Flow 반환
- `./gradlew :data:compileDebugKotlin` 성공 필수

## 완료 조건
- [ ] 13개 Entity 전부 컴파일 성공
- [ ] 모든 DAO 컴파일 성공
- [ ] 모든 Repository Impl 컴파일 성공
- [ ] Room schema export 확인
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 3 프롬프트 (Scan System)

```
docs/Stack_SSOT.md를 읽어라. Phase 2까지 완료된 상태다.

지금부터 Phase 3 (Scan System)을 구현한다. SSOT Section 7 전체를 참고하라.

## 할 일

1. **MediaStoreScanner**: SAF treeUri 재귀 탐색, 메타데이터 추출 (MediaMetadataRetriever)
2. **LrcFileScanner**: 같은 폴더에서 동일 파일명.lrc 파일 탐색 및 파싱 (SSOT Section 12.2)
3. **ScanManager**: Mutex 기반 단일 파이프라인, 증분/풀 스캔 분기, ScanProgress Flow emit
4. **ContentObserverManager**: MediaStore 변경 감지 + 3초 디바운스
5. **UseCase 구현**: ScanLibraryUseCase, RescanLibraryUseCase, CleanGhostTracksUseCase
6. **Ghost 정책**: SSOT Section 7.4 그대로 구현

## 규칙
- 스캔은 Dispatchers.IO에서 실행, 취소 가능
- 동시 스캔 요청은 Mutex로 하나만 실행
- 확장자 화이트리스트: mp3, flac, m4a, ogg, opus, wav, aac, wma, aiff

## 완료 조건
- [ ] ScanManager가 SAF URI로 오디오 파일을 정상 탐색
- [ ] 메타데이터 추출 후 Room DB에 Upsert
- [ ] FTS 인덱스 갱신
- [ ] Ghost 감지 동작
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 4 프롬프트 (Gate & Onboarding)

```
docs/Stack_SSOT.md를 읽어라. Phase 3까지 완료된 상태다.

지금부터 Phase 4 (Gate & Onboarding)를 구현한다. SSOT Section 5 전체를 참고하라.

## 할 일

1. **GateScreen**: 카드 스와이프 온보딩 (환영 → 권한 → 알림 → 폴더 → 스캔)
2. **GateViewModel**: MVI 패턴, GateReady 상태 머신 (SSOT Section 5.2)
3. **PermissionManager**: API 레벨별 권한 처리 (READ_MEDIA_AUDIO vs READ_EXTERNAL_STORAGE)
4. **SAF 폴더 선택**: Tree URI 선택 + takePersistableUriPermission + SourceFolderEntity 저장
5. **스캔 진행률 UI**: ScanProgress Flow 구독 → 프로그레스바 + 카운트 표시
6. **GateReady 영속화**: DataStore에 gate_completed 저장
7. **실패 UX**: SSOT Section 5.3 (한 줄 설명 + 액션 1개)

## 규칙
- SSOT Section 2.1 GateReady 규칙 엄격 준수
- GateReady 전에는 라이브러리/플레이어 UI 절대 노출 금지
- strings.xml에 모든 텍스트 리소스화

## 완료 조건
- [ ] 앱 최초 실행 시 GateScreen 표시
- [ ] 권한 요청 → 폴더 선택 → 스캔 진행률 → 완료 플로우 동작
- [ ] GateReady 후 재실행 시 Gate 건너뜀
- [ ] 권한 거부 시 에러 UI 정상 표시
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 5 프롬프트 (Playback Engine)

```
docs/Stack_SSOT.md를 읽어라. Phase 4까지 완료된 상태다.

지금부터 Phase 5 (Playback Engine)를 구현한다. SSOT Section 9 전체를 참고하라.

## 할 일

1. **PlaybackCommand**: SSOT Section 9.4의 sealed interface 그대로 구현
2. **PlaybackState**: SSOT Section 9.3의 data class 그대로 구현
3. **PlaybackStateReducer**: StateFlow<PlaybackState> SSOT
4. **CommandDispatcher**: Mutex 기반 직렬화 (SSOT Section 9.8)
5. **PlaybackQueue**: original + shuffled 리스트 관리, Fisher-Yates, AddNext
6. **StackPlayerManager**: Dual ExoPlayer (A/B), gapless 프리로드
7. **CrossfadeController**: 3-branch 로직 (SSOT Section 9.5)
8. **AudioFocusHandler**: gain/loss/duck/transient
9. **ABRepeatController**: A/B 포인트 + 자동 시크 (SSOT Section 15)
10. **EqualizerManager**: AudioEffect EQ 바인딩 (SSOT Section 14.1)
11. **세션 저장/복원**: PlaybackSessionEntity 읽기/쓰기 (SSOT Section 9.9)

## 규칙
- :core:player는 UI 의존성 제로
- 모든 상태 변경은 CommandDispatcher를 통과 (SSOT Section 2.3)
- Player 콜백도 InternalCommand로 변환하여 같은 파이프라인 통과
- Crossfade/Gapless 충돌 시 자동 해제 + 이유 표시 (Section 2.4)

## 완료 조건
- [ ] 단일 트랙 재생/정지/시크 동작
- [ ] 큐 기반 연속 재생 + 셔플/리피트
- [ ] Dual ExoPlayer 교대 (gapless) 동작
- [ ] CommandDispatcher 직렬화 검증 (동시 명령 충돌 없음)
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 6 프롬프트 (Core UI)

```
docs/Stack_SSOT.md를 읽어라. Phase 5까지 완료된 상태다.

지금부터 Phase 6 (Core UI)를 구현한다. SSOT Section 19, 20을 참고하라.

## 할 일

1. **MainActivity**: Compose host + WindowSizeClass 감지
2. **StackNavHost**: SSOT Section 20.1의 모든 라우트 등록
3. **LibraryShellScreen**: Scaffold + BottomNav(4탭) + MiniPlayer 호스트
4. **TracksTab**: LazyColumn + Sort/Filter + Pull-to-refresh + Empty state
5. **AlbumsTab**: LazyVerticalGrid + 앨범아트
6. **ArtistsTab**: LazyColumn + 트랙/앨범 카운트
7. **FoldersTab**: Breadcrumb 네비게이션 + 파일/폴더 리스트
8. **AlbumDetailScreen**: 앨범아트 + 트랙 목록 + 전체재생
9. **ArtistDetailScreen**: 아티스트 앨범/트랙 목록
10. **MiniPlayer**: SSOT Section 19.5 (아트/제목/컨트롤 + 스와이프)
11. **NowPlayingScreen**: SSOT Section 19.6 (풀스크린 플레이어)
12. **QueueSheet**: BottomSheet (드래그정렬 + 스와이프삭제)
13. **StackMediaService**: MediaSessionService + 알림 (SSOT Section 16)
14. **PlayerViewModel**: :core:player 상태를 UI에 브릿지
15. **Adaptive Layout**: Expanded DualPane (SSOT Section 19.2)

## 규칙
- GateReady 전에는 Main 화면 접근 불가 (NavHost에서 gate → main 분기)
- PlaybackViewModel은 단일 인스턴스 (SSOT Section 19.3)
- MiniPlayer는 Expanded 모드에서 제거
- 모든 텍스트 strings.xml 리소스

## 완료 조건
- [ ] Gate 통과 후 라이브러리 4탭 표시
- [ ] 트랙 탭 → 곡 선택 → 재생 시작 → MiniPlayer 표시
- [ ] MiniPlayer 탭 → NowPlaying 화면
- [ ] 앨범/아티스트 상세 화면 정상 동작
- [ ] 알림에서 재생/정지/스킵 동작
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 7 프롬프트 (Tags & Playlists)

```
docs/Stack_SSOT.md를 읽어라. Phase 6까지 완료된 상태다.

지금부터 Phase 7 (Tags & Playlists)를 구현한다. SSOT Section 10, 11을 참고하라.

## 할 일

### Tag System (Section 10)
1. **시스템 태그 4종 시드**: 앱 최초 실행 시 FAVORITE/RECENT_PLAY/RECENT_ADD/MOST_PLAYED 생성
2. **ToggleFavoriteUseCase**: 하트 토글 → TrackTagCrossRef insert/delete
3. **RefreshSystemTagsUseCase**: 재생기록/스캔 완료 시 자동 갱신
4. **TagsScreen**: 태그 목록/그리드, 시스템 태그 보호(삭제 불가)
5. **TagDetailScreen**: 태그에 속한 트랙 목록
6. **TagEditorDialog**: 이름 + 색상 피커
7. **라이브러리 태그 필터**: AND/OR 토글

### Playlist System (Section 11)
8. **PlaylistsScreen**: 플레이리스트 목록
9. **PlaylistDetailScreen**: 트랙 + 드래그 순서변경 + 스와이프 삭제
10. **PlaylistEditorDialog**: 생성/수정 (이름 + 설명)
11. **트랙 추가 다이얼로그**: 트랙 선택 UI
12. **플레이리스트 재생**: 큐 생성 로직

### 통합
13. **NowPlaying에 하트 버튼** 연결
14. **MiniPlayer에 하트 아이콘** 표시
15. **알림에 Favorite 커스텀 커맨드** 연결
16. **트랙 롱프레스 메뉴**: "플레이리스트에 추가", "태그 부여"

## 완료 조건
- [ ] 시스템 태그 4종 자동 생성 + 자동 갱신
- [ ] 커스텀 태그 CRUD + 트랙 부여/해제
- [ ] 하트 토글이 NowPlaying/MiniPlayer/알림에서 동작
- [ ] 플레이리스트 CRUD + 트랙 추가/제거/순서변경
- [ ] 플레이리스트 재생 시 큐 정상 생성
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 8 프롬프트 (Search, Lyrics, EQ, AB Repeat)

```
docs/Stack_SSOT.md를 읽어라. Phase 7까지 완료된 상태다.

지금부터 Phase 8을 구현한다. SSOT Section 12, 13, 14, 15를 참고하라.

## 할 일

### Search (Section 13)
1. **SearchScreen**: 검색바 + 최근검색어 + 결과 (트랙/앨범/아티스트 그룹)
2. **SearchViewModel**: 300ms 디바운스 + FTS 쿼리
3. **최근 검색어**: DataStore 저장/표시/삭제

### Lyrics (Section 12)
4. **LyricsOverlay**: NowPlaying에서 싱크 스크롤 (SYNCED) / 정적 표시 (PLAIN)
5. **가사 라인 탭 → 시크**: 타임스탬프 위치로 seekTo
6. **LyricsEditorScreen**: LRC 파일 임포트(SAF) + 텍스트 편집 + 싱크 도구 + 익스포트

### Equalizer (Section 14)
7. **EqualizerScreen**: 밴드 슬라이더 + 시스템/커스텀 프리셋 + ON/OFF 토글
8. **EqualizerManager 연결**: ExoPlayer 세션 ID 바인딩

### A-B Repeat (Section 15)
9. **ABRepeatControls**: NowPlaying Seekbar에 A/B 마커 표시
10. **A 설정 / B 설정 / 클리어 버튼**
11. **자동 루프**: B 도달 시 CommandDispatcher 통해 A로 시크

## 완료 조건
- [ ] 검색 디바운스 + FTS 결과 표시 + 결과 탭 재생/이동
- [ ] 싱크 가사 자동 스크롤 + 라인 탭 시크
- [ ] LRC 임포트/편집/익스포트 동작
- [ ] EQ 밴드 조절 시 오디오에 반영
- [ ] AB 반복 설정 → B 도달 시 A로 자동 이동
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 9 프롬프트 (Settings, Backup, Deletion, Deep Links)

```
docs/Stack_SSOT.md를 읽어라. Phase 8까지 완료된 상태다.

지금부터 Phase 9를 구현한다. SSOT Section 17, 18, 20.2, 21을 참고하라.

## 할 일

### Settings (Section 21)
1. **SettingsScreen**: 전체 카테고리 구현 (외관/재생/라이브러리/저장소/데이터/정보)
2. **ScanFolderScreen**: SAF 폴더 추가/제거 + 재스캔
3. **테마 즉시 반영**: System/Light/Dark 전환
4. **Gapless/Crossfade 충돌 해소 + Snackbar**

### Backup & Restore (Section 18)
5. **BackupRestoreScreen**: 백업 생성 / 복원 실행 UI
6. **BackupManager**: JSON export (metadata, playlists, tags, settings, history, lyrics)
7. **RestoreManager**: JSON import + merge 정책 (SSOT Section 18.2)

### File Deletion (Section 17)
8. **DeleteTrackUseCase**: API 29 RecoverableSecurityException / API 30+ createDeleteRequest
9. **현재 재생 중 삭제 안전 처리** (SSOT Section 17.3)
10. **DB/FTS/Playlist/Tag cascade 정리**

### Deep Links (Section 20.2)
11. **AndroidManifest intent-filter**: stack:// 스키마
12. **NavHost deep link 처리**: GateReady 미통과 시 pending 저장

## 완료 조건
- [ ] 모든 설정 항목 동작 + 영속화
- [ ] 백업 생성 → 유효한 JSON 파일 확인
- [ ] 복원 → 데이터 정상 merge
- [ ] 파일 삭제 (API 29/30+) → DB 정리 + 재생 안전
- [ ] Deep link로 앱 진입 시 올바른 화면 이동
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
```

---

## Phase 10 프롬프트 (Polish & i18n)

```
docs/Stack_SSOT.md를 읽어라. Phase 9까지 완료된 상태다.

지금부터 Phase 10 (최종 마무리)을 구현한다. SSOT Section 24, 19.2, 23, 26을 참고하라.

## 할 일

### i18n (Section 24)
1. **values-ko/strings.xml**: 모든 영어 string의 한국어 번역
2. **values-ja/strings.xml**: 모든 영어 string의 일본어 번역
3. **시스템 태그 이름 현지화**: FAVORITE → 즐겨찾기 / お気に入り 등

### Adaptive UI (Section 19.2)
4. **Expanded DualPane**: WindowSizeClass.Expanded에서 좌우 분할 레이아웃
5. **MiniPlayer 제거** (Expanded)
6. **PlaybackViewModel 단일 인스턴스** 양쪽 패널 연동 확인

### Diagnostics (Section 23)
7. **CrashLogScreen**: 크래시 목록 + 상세 (stacktrace 펼침)
8. **CrashCapture 연결**: UncaughtExceptionHandler → Room 저장

### Tutorial
9. **Tutorial Overlay**: 최초 GateReady 후 1회 표시 (SSOT Section 19.9)

### 최종 점검
10. **Acceptance Criteria** (SSOT Section 26) 전체 항목 체크
11. **hardcoded string 검사**: grep으로 Composable 내 리터럴 문자열 없는지 확인
12. **Lint 정리**: 주요 warning 제거

## 완료 조건
- [ ] 한국어/일본어로 언어 변경 시 모든 UI 정상 표시
- [ ] Expanded 모드에서 DualPane 동작 + MiniPlayer 없음
- [ ] 크래시 발생 후 재실행 → CrashLogScreen에서 확인 가능
- [ ] SSOT Section 26 Acceptance Criteria 전체 통과
- [ ] `./gradlew assembleDebug` BUILD SUCCESSFUL
- [ ] 앱이 정상적으로 설치, 실행, 재생까지 동작
