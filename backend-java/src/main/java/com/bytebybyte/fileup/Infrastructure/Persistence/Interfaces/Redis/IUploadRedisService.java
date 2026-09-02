package com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Redis;

import java.util.Set;
import java.util.UUID;

public interface IUploadRedisService {
    String createUploadedChunksKey(UUID sessionId);
    String createChunkHashesKey(UUID sessionId);
    void registerChunk(UUID sessionId, int chunkIndex, String hashChunk);
    void removeChunk(UUID sessionId, int chunkIndex);
    Set<Integer> getUploadedChunks(UUID sessionId);
    void isChunkPresent(String chunkHashKey, int chunkIndex);
    String createChunkHashKey(UUID sessionId, int chunkIndex, String hashChunk);
}
