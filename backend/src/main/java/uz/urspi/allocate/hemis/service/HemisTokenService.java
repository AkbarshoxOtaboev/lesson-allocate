package uz.urspi.allocate.hemis.service;

import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.response.HemisTokenResponse;

public interface HemisTokenService {

    HemisTokenResponse getTokenInfo();

    HemisTokenResponse saveToken(HemisTokenRequest request);

    void deleteToken();
}
