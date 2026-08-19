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
