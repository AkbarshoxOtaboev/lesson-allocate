package uz.urspi.allocate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.repository.SubjectRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectHoursBackfill implements ApplicationRunner {

    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updated = 0;
        for (Subject subject : subjectRepository.findAll()) {
            int lecture = nz(subject.getLectureHours());
            int practical = nz(subject.getPracticalHours());
            int lab = nz(subject.getLabHours());
            int seminar = nz(subject.getSeminarHours());
            int independent = nz(subject.getIndependentStudyHours());
            int rating = nz(subject.getRatingHours());
            int auditorium = lecture + practical + lab + seminar + rating;
            int total = auditorium + independent;
            boolean dirty = !Integer.valueOf(auditorium).equals(nz(subject.getAuditoriumHours()))
                    || !Integer.valueOf(total).equals(nz(subject.getTotalHours()));
            if (dirty) {
                subject.setAuditoriumHours(auditorium);
                subject.setTotalHours(total);
                subjectRepository.save(subject);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Subject hours backfill: {} rows updated", updated);
        }
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
