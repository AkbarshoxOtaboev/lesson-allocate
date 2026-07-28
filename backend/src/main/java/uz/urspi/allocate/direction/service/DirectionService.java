package uz.urspi.allocate.direction.service;

import uz.urspi.allocate.direction.dto.DirectionRequest;
import uz.urspi.allocate.direction.response.DirectionResponse;

import java.util.List;

public interface DirectionService {
    DirectionResponse create(DirectionRequest request);
    List<DirectionResponse> findAll();
    DirectionResponse findById(Long id);
    DirectionResponse update(Long id, DirectionRequest request);
    void delete(Long id);
}
