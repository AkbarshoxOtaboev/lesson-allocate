package uz.urspi.allocate.hemis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.urspi.allocate.common.entity.BaseEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "external_tokens", uniqueConstraints = @UniqueConstraint(columnNames = "provider"))
@SQLRestriction("status <> 'DELETED'")
public class ExternalToken extends BaseEntity {

    public static final String PROVIDER_HEMIS = "HEMIS";

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 64)
    private String provider;

    @Lob
    @Column(nullable = false)
    private String accessToken;

    private String baseUrl;

    private String description;
}
