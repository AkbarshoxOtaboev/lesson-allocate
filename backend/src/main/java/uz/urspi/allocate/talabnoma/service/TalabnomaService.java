package uz.urspi.allocate.talabnoma.service;

import uz.urspi.allocate.talabnoma.dto.TalabnomaRejectRequest;
import uz.urspi.allocate.talabnoma.dto.TalabnomaRequest;
import uz.urspi.allocate.talabnoma.enums.TalabnomaStatus;
import uz.urspi.allocate.talabnoma.response.TalabnomaResponse;
import uz.urspi.allocate.talabnoma.response.TalabnomaStatsResponse;

import java.util.List;

public interface TalabnomaService {

    TalabnomaResponse create(TalabnomaRequest request);

    TalabnomaResponse update(Long id, TalabnomaRequest request);

    List<TalabnomaResponse> findAll(Long facultyId, Long departmentId, TalabnomaStatus status);

    TalabnomaResponse findById(Long id);

    TalabnomaResponse accept(Long id);

    TalabnomaResponse reject(Long id, TalabnomaRejectRequest request);

    void delete(Long id);

    TalabnomaStatsResponse stats(Long facultyId, Long departmentId);

    long countNewForCurrentUser();

    /** Fan taqsimoti o'zgarganda bog'langan talabnoma holatini yangilash */
    void refreshStatusForSubject(Long subjectId);
}
