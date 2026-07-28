package uz.urspi.allocate.talabnoma.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TalabnomaStatsResponse {
    private long total;
    private long pending;
    private long accepted;
    private long rejected;
    private long allocated;
}
