package usyd.library_reservation_system.library_reservation_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.model.Label;
import usyd.library_reservation_system.library_reservation_system.repository.LabelRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;


    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }


    public Optional<Label> getLabelById(Integer id) {
        return labelRepository.findById(id);
    }


    public Optional<Label> getLabelByName(String labelName) {
        return labelRepository.findByLabelName(labelName);
    }


    public Label createLabel(Label label) {
        if (labelRepository.existsByLabelName(label.getLabelName())) {
            throw new RuntimeException("The label name already exists: " + label.getLabelName());
        }


        if (label.getCreateDate() == null) {
            label.setCreateDate(LocalDateTime.now());
        }

        return labelRepository.save(label);
    }


    public Label updateLabel(Integer id, Label labelDetails) {
        Optional<Label> optionalLabel = labelRepository.findById(id);
        if (optionalLabel.isPresent()) {
            Label label = optionalLabel.get();


            if (!label.getLabelName().equals(labelDetails.getLabelName()) &&
                    labelRepository.existsByLabelName(labelDetails.getLabelName())) {
                throw new RuntimeException("The label name already exists: " + labelDetails.getLabelName());
            }

            label.setLabelName(labelDetails.getLabelName());
            return labelRepository.save(label);
        }
        return null;
    }


    public boolean deleteLabel(Integer id) {
        if (labelRepository.existsById(id)) {
            labelRepository.deleteById(id);
            return true;
        }
        return false;
    }


    public List<Label> searchLabelsByName(String labelName) {
        return labelRepository.findByLabelNameContaining(labelName);
    }


    public List<Label> getAllLabelsByCreateDate() {
        return labelRepository.findAllOrderByCreateDateDesc();
    }


    public List<Label> getAllLabelsByName() {
        return labelRepository.findAllOrderByLabelNameAsc();
    }
}