package com.bytebybyte.fileup.Application.Services.UploadRedisService;


import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadRedisService {

    private final RedisTemplate<String, Object> _redisTemplate;
    private static final String CHUNKS_KEY = "upload:%s:chunks";
    private static final String HASHES_KEY = "upload:%s:hashes";





    /**
     * Creates the Redis key responsible for storing
     * the indexes of uploaded chunks for an upload session.
     * Example:
     * upload:UUID:chunks
     * @param sessionId session identifier
     * @return String type Redis key
     */
    public String createUploadedChunksKey(UUID sessionId) {
        return String.format(CHUNKS_KEY, sessionId);
    }


    /**
     * Creates the Redis key responsible for storing
     * the hashes of uploaded chunks for an upload session.
     * Example:
     * upload:UUID:hashes
     * @param sessionId session identifier
     * @return String type Redis key
     */
    public String createChunkHashesKey(UUID sessionId) {
        return String.format(HASHES_KEY, sessionId);
    }


    /**
     * Atomically registers a chunk index in Redis.
     * SADD returns:
     * 1 -> chunk was added successfully
     * 0 -> chunk already exists
     * @param sessionId session identifier
     * @param chunkIndex chunk index being uploaded
     * @param hashChunk hash of the chunk
     */
    public void registerChunk(
            UUID sessionId,
            int chunkIndex,
            String hashChunk) {

        String chunksKey = createUploadedChunksKey(sessionId);
        String hashesKey = createChunkHashesKey(sessionId);

        String index = String.valueOf(chunkIndex);

        Long added = _redisTemplate
                .opsForSet()
                .add(chunksKey, index);

        if (added == null || added == 0) {
            throw new ConflictException(
                    "Chunk already present on Redis",
                    "UploadRedisService_registerChunk_method");
        }

        _redisTemplate
                .opsForHash()
                .put(hashesKey, index, hashChunk);
    }


    /**
     * Removes a chunk index from Redis.
     * Used when the chunk was registered, but
     * the physical file could not be created.
     * @param sessionId session identifier
     * @param chunkIndex chunk index being removed
     */
    public void removeChunk(
            UUID sessionId,
            int chunkIndex) {

        String chunksKey = createUploadedChunksKey(sessionId);

        _redisTemplate
                .opsForSet()
                .remove(chunksKey, String.valueOf(chunkIndex));
    }


    /**
     * Method to check if a chunk is already present in the Redis set, if so, throw a ConflictException.
     *
     * @param chunkHashKey Redis key for the chunk hash validation
     * @param chunkIndex chunk index being uploaded
     */
    public void isChunkPresent(String chunkHashKey, int chunkIndex){
        String index = String.valueOf(chunkIndex);

        if (Boolean.TRUE.equals(_redisTemplate.opsForSet().isMember(chunkHashKey, index))){
            throw new ConflictException("Chunk already present on Redis", "UploadRedisService_isChunkPresent_method");
        }
    }

    /**
     * Method to create the Redis key for the chunk hash validation.
     * @param sessionId session identifier
     * @param chunkIndex chunk index being uploaded
     * @param hashChunk hash of the chunk
     * @return String type Redis key
     */
    public String createChunkHashKey(UUID sessionId, int chunkIndex, String hashChunk){
        String id = String.valueOf(sessionId);
        String hash = String.valueOf(hashChunk);
        String index = String.valueOf(chunkIndex);

        return String.format("chunkKey:%s:%s:%s", id, hash, index);
    }

}
