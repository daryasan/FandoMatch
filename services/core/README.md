# Core Service

Core service handles user profiles and matching logic.

## Endpoints

### User Profile

#### `POST /core/user/profile` — getProfile

```mermaid
sequenceDiagram
    participant Client
    participant ProfilesController
    participant TokenParserService
    participant ProfilesService
    participant UserProfileRepository
    participant UsersAdapter
    participant FandomService
    participant MatchesService
    participant ProfileStrategyFactory

    Client->>ProfilesController: POST /core/user/profile (token, username)
    ProfilesController->>TokenParserService: parse(token)
    TokenParserService-->>ProfilesController: UserTokenData(userId)
    ProfilesController->>ProfilesService: getProfile(uuid, username)
    ProfilesService->>UserProfileRepository: findByUsername(username)
    UserProfileRepository-->>ProfilesService: UserProfile
    ProfilesService->>MatchesService: areFriends(currentUuid, targetUuid)
    MatchesService-->>ProfilesService: Boolean
    alt profileType == OWN
        ProfilesService->>UsersAdapter: getUserCredentialsByUuid(currentUuid)
        UsersAdapter-->>ProfilesService: UserCredentials
    end
    ProfilesService->>FandomService: getFandoms(targetUserId)
    FandomService-->>ProfilesService: List<Fandom>
    ProfilesService->>ProfileStrategyFactory: getStrategy(profileType, profileData)
    ProfileStrategyFactory-->>ProfilesService: ConstructProfileStrategy
    ProfilesService-->>ProfilesController: UserProfileResponse
    ProfilesController-->>Client: 200 OK (UserProfileResponse)
```

#### `PATCH /core/user/profile/edit` — editProfile

```mermaid
sequenceDiagram
    participant Client
    participant ProfilesController
    participant TokenParserService
    participant ProfilesService
    participant UserProfileRepository
    participant UsersAdapter
    participant FandomService
    participant ProfileStrategyFactory

    Client->>ProfilesController: PATCH /core/user/profile/edit (token, fields)
    ProfilesController->>TokenParserService: parse(token)
    TokenParserService-->>ProfilesController: UserTokenData(userId)
    ProfilesController->>ProfilesService: editProfile(uuid, request)
    ProfilesService->>UserProfileRepository: findById(uuid)
    UserProfileRepository-->>ProfilesService: UserProfile
    ProfilesService->>UserProfileRepository: save(updated)
    ProfilesService->>UsersAdapter: getUserCredentialsByUuid(uuid)
    UsersAdapter-->>ProfilesService: UserCredentials
    ProfilesService->>FandomService: getFandoms(uuid)
    FandomService-->>ProfilesService: List<Fandom>
    ProfilesService->>ProfileStrategyFactory: getStrategy(OWN, profileData)
    ProfileStrategyFactory-->>ProfilesService: ConstructOwnProfile
    ProfilesService-->>ProfilesController: EditUserProfileResponse
    ProfilesController-->>Client: 200 OK (EditUserProfileResponse)
```

### Matching

#### `POST /core/match/next` — getNextCandidates

```mermaid
sequenceDiagram
    participant Client
    participant MatchController
    participant TokenParserService
    participant MatchesService
    participant MatchFilterRepository
    participant UserProfileRepository
    participant FandomService
    participant MatchPendingRepository

    Client->>MatchController: POST /core/match/next (token, batchSize)
    MatchController->>TokenParserService: parse(token)
    TokenParserService-->>MatchController: UserTokenData(userId)
    MatchController->>MatchesService: getNextCandidates(userId, batchSize)
    MatchesService->>MatchFilterRepository: findById(userId)
    MatchFilterRepository-->>MatchesService: MatchFilter
    MatchesService->>UserProfileRepository: findCandidates(userId, filters, poolSize)
    UserProfileRepository-->>MatchesService: List<UserProfile>
    MatchesService->>FandomService: getFandoms(currentUserId)
    FandomService-->>MatchesService: List<Fandom> (fetched once)
    loop for each candidate
        MatchesService->>FandomService: getFandoms(candidateId)
        FandomService-->>MatchesService: List<Fandom>
        Note over MatchesService: calculateCompatibility(userFandoms, candidateFandoms)
    end
    MatchesService->>MatchPendingRepository: saveAll(pending)
    MatchesService-->>MatchController: MatchCandidateBatchResponse
    MatchController-->>Client: 200 OK (MatchCandidateBatchResponse)
```

#### `POST /core/match/react` — react

```mermaid
sequenceDiagram
    participant Client
    participant MatchController
    participant TokenParserService
    participant MatchesService
    participant UserProfileRepository
    participant MatchActionRepository
    participant MatchPendingRepository
    participant MatchRepository
    participant MatchEventProducer

    Client->>MatchController: POST /core/match/react (token, targetUuid, action)
    MatchController->>TokenParserService: parse(token)
    TokenParserService-->>MatchController: UserTokenData(userId)
    MatchController->>MatchesService: react(userId, targetUuid, action)
    MatchesService->>UserProfileRepository: existsById(targetUuid)
    UserProfileRepository-->>MatchesService: Boolean
    MatchesService->>MatchActionRepository: findByUserIdAndTargetUserId(userId, targetUuid)
    MatchActionRepository-->>MatchesService: null (no prior action)
    MatchesService->>MatchActionRepository: save(MatchAction)
    MatchesService->>MatchPendingRepository: deleteByUserIdAndSuggestedUserId(userId, targetUuid)
    MatchesService->>MatchActionRepository: findByUserIdAndTargetUserId(targetUuid, userId)
    MatchActionRepository-->>MatchesService: MatchAction? (opposite action)
    alt mutual LIKE
        MatchesService->>MatchRepository: save(Match)
        MatchesService->>MatchEventProducer: sendMatchEvent(matchId, user1, user2)
        MatchesService-->>MatchController: MatchActionResponse(MATCH)
    else not mutual
        MatchesService-->>MatchController: MatchActionResponse(LIKED/DISLIKED)
    end
    MatchController-->>Client: 200 OK (MatchActionResponse)
```

#### `POST /core/match/filter` — setFilter

```mermaid
sequenceDiagram
    participant Client
    participant MatchController
    participant TokenParserService
    participant MatchesService
    participant MatchFilterRepository

    Client->>MatchController: POST /core/match/filter (token, filter params)
    MatchController->>TokenParserService: parse(token)
    TokenParserService-->>MatchController: UserTokenData(userId)
    MatchController->>MatchesService: setFilter(userId, request)
    MatchesService->>MatchFilterRepository: save(MatchFilter)
    MatchesService-->>MatchController: MatchFilterResponse(SUCCESS)
    MatchController-->>Client: 200 OK (MatchFilterResponse)
```

