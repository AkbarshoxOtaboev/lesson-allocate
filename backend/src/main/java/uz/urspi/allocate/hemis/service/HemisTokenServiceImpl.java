package uz.urspi.allocate.hemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.config.HemisProperties;
import uz.urspi.allocate.hemis.dto.HemisTokenRequest;
import uz.urspi.allocate.hemis.entity.ExternalToken;
import uz.urspi.allocate.hemis.repository.ExternalTokenRepository;
import uz.urspi.allocate.hemis.response.HemisTokenResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class HemisTokenServiceImpl implements HemisTokenService {

    private final ExternalTokenRepository externalTokenRepository;
    private final HemisProperties hemisProperties;

    @Override
    @Transactional(readOnly = true)
    public HemisTokenResponse getTokenInfo() {
        return externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .map(this::toResponse)
                .orElseGet(() -> HemisTokenResponse.builder()
                        .provider(ExternalToken.PROVIDER_HEMIS)
                        .configured(false)
                        .baseUrl(hemisProperties.getBaseUrl())
                        .build());
    }

    @Override
    public HemisTokenResponse saveToken(HemisTokenRequest request) {
        ExternalToken token = externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .orElseGet(() -> ExternalToken.builder()
                        .provider(ExternalToken.PROVIDER_HEMIS)
                        .build());

        token.setAccessToken(request.getAccessToken().trim());
        if (StringUtils.hasText(request.getBaseUrl())) {
            token.setBaseUrl(request.getBaseUrl().trim());
        } else if (token.getBaseUrl() == null) {
            token.setBaseUrl(hemisProperties.getBaseUrl());
        }
        if (request.getDescription() != null) {
            token.setDescription(request.getDescription());
        }
        if (token.getId() == null) {
            token.setCreatedUsername(SecurityUtils.getCurrentUsername());
        }
        return toResponse(externalTokenRepository.save(token));
    }

    @Override
    public void deleteToken() {
        externalTokenRepository.findByProvider(ExternalToken.PROVIDER_HEMIS)
                .ifPresent(token -> {
                    token.softDelete();
                    externalTokenRepository.save(token);
                });
    }

    private HemisTokenResponse toResponse(ExternalToken token) {
        return HemisTokenResponse.builder()
                .id(token.getId())
                .provider(token.getProvider())
                .maskedToken(mask(token.getAccessToken()))
                .configured(StringUtils.hasText(token.getAccessToken()))
                .baseUrl(token.getBaseUrl() != null ? token.getBaseUrl() : hemisProperties.getBaseUrl())
                .description(token.getDescription())
                .updatedAt(token.getUpdatedAt())
                .build();
    }

    private String mask(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
