package uz.urspi.allocate.hemis.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HemisSyncResult {

    private int fetched;
    private int created;
    private int updated;
    private int skipped;
}
