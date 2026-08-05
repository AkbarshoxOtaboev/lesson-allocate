package uz.urspi.allocate.hemis.response;

import lombok.Builder;
import lombok.Getter;
import uz.urspi.allocate.hemis.dto.HemisGroupDto;

import java.util.List;

@Getter
@Builder
public class HemisGroupListResponse {

    private List<HemisGroupDto> items;
    private Integer page;
    private Integer pageCount;
    private Integer totalCount;
    private Integer pageSize;
}
