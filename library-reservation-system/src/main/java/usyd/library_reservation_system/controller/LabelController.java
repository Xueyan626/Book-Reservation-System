
package usyd.library_reservation_system.library_reservation_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import usyd.library_reservation_system.library_reservation_system.model.Label;
import usyd.library_reservation_system.library_reservation_system.service.LabelService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/labels")
@CrossOrigin(origins = "*")
public class LabelController {

    @Autowired
    private LabelService labelService;


    @GetMapping
    public ResponseEntity<List<Label>> getAllLabels() {
        try {
            List<Label> labels = labelService.getAllLabels();
            return ResponseEntity.ok(labels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Label> getLabelById(@PathVariable Integer id) {
        try {
            Optional<Label> label = labelService.getLabelById(id);
            if (label.isPresent()) {
                return ResponseEntity.ok(label.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/name/{labelName}")
    public ResponseEntity<Label> getLabelByName(@PathVariable String labelName) {
        try {
            Optional<Label> label = labelService.getLabelByName(labelName);
            if (label.isPresent()) {
                return ResponseEntity.ok(label.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping
    public ResponseEntity<Object> createLabel(@RequestBody Label label) {
        try {
            Label createdLabel = labelService.createLabel(label);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLabel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("创建标签失败"));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Object> updateLabel(@PathVariable Integer id, @RequestBody Label labelDetails) {
        try {
            Label updatedLabel = labelService.updateLabel(id, labelDetails);
            if (updatedLabel != null) {
                return ResponseEntity.ok(updatedLabel);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("更新标签失败"));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteLabel(@PathVariable Integer id) {
        try {
            boolean deleted = labelService.deleteLabel(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("删除标签失败"));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Label>> searchLabels(@RequestParam String name) {
        try {
            List<Label> labels = labelService.searchLabelsByName(name);
            return ResponseEntity.ok(labels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/ordered/create-date")
    public ResponseEntity<List<Label>> getLabelsByCreateDate() {
        try {
            List<Label> labels = labelService.getAllLabelsByCreateDate();
            return ResponseEntity.ok(labels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/ordered/name")
    public ResponseEntity<List<Label>> getLabelsByName() {
        try {
            List<Label> labels = labelService.getAllLabelsByName();
            return ResponseEntity.ok(labels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    public static class LabelNameRequest {
        private String labelName;

        public String getLabelName() {
            return labelName;
        }

        public void setLabelName(String labelName) {
            this.labelName = labelName;
        }
    }

    public static class BatchLabelRequest {
        private List<String> labelNames;

        public List<String> getLabelNames() {
            return labelNames;
        }

        public void setLabelNames(List<String> labelNames) {
            this.labelNames = labelNames;
        }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}