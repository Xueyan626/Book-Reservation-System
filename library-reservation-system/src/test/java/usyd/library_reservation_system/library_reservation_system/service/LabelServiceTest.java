package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import usyd.library_reservation_system.library_reservation_system.model.Label;
import usyd.library_reservation_system.library_reservation_system.repository.LabelRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private LabelService labelService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllLabels() {
        Label label1 = createTestLabel(1, "Programming", LocalDateTime.now());
        Label label2 = createTestLabel(2, "Database", LocalDateTime.now());
        List<Label> mockLabels = Arrays.asList(label1, label2);

        when(labelRepository.findAll()).thenReturn(mockLabels);

        List<Label> result = labelService.getAllLabels();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(labelRepository, times(1)).findAll();
    }

    @Test
    void testGetLabelById_Exists() {
        Label label = createTestLabel(1, "Programming", LocalDateTime.now());

        when(labelRepository.findById(1)).thenReturn(Optional.of(label));

        Optional<Label> result = labelService.getLabelById(1);

        assertTrue(result.isPresent());
        assertEquals("Programming", result.get().getLabelName());
        verify(labelRepository, times(1)).findById(1);
    }

    @Test
    void testGetLabelById_NotExists() {
        when(labelRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Label> result = labelService.getLabelById(999);

        assertFalse(result.isPresent());
        verify(labelRepository, times(1)).findById(999);
    }

    @Test
    void testGetLabelByName_Exists() {
        Label label = createTestLabel(1, "Programming", LocalDateTime.now());

        when(labelRepository.findByLabelName("Programming")).thenReturn(Optional.of(label));

        Optional<Label> result = labelService.getLabelByName("Programming");

        assertTrue(result.isPresent());
        assertEquals("Programming", result.get().getLabelName());
        verify(labelRepository, times(1)).findByLabelName("Programming");
    }

    @Test
    void testGetLabelByName_NotExists() {
        when(labelRepository.findByLabelName("NonExistent")).thenReturn(Optional.empty());

        Optional<Label> result = labelService.getLabelByName("NonExistent");

        assertFalse(result.isPresent());
        verify(labelRepository, times(1)).findByLabelName("NonExistent");
    }

    @Test
    void testCreateLabel_Success() {
        Label label = new Label();
        label.setLabelName("New Label");

        Label savedLabel = createTestLabel(1, "New Label", LocalDateTime.now());

        when(labelRepository.existsByLabelName("New Label")).thenReturn(false);
        when(labelRepository.save(any(Label.class))).thenReturn(savedLabel);

        Label result = labelService.createLabel(label);

        assertNotNull(result);
        assertEquals("New Label", result.getLabelName());
        verify(labelRepository, times(1)).existsByLabelName("New Label");
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    void testCreateLabel_DuplicateName() {
        Label label = new Label();
        label.setLabelName("Existing Label");

        when(labelRepository.existsByLabelName("Existing Label")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> labelService.createLabel(label));
        verify(labelRepository, times(1)).existsByLabelName("Existing Label");
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void testCreateLabel_WithNullCreateDate() {
        Label label = new Label();
        label.setLabelName("Test Label");
        label.setCreateDate(null);

        Label savedLabel = createTestLabel(1, "Test Label", LocalDateTime.now());

        when(labelRepository.existsByLabelName("Test Label")).thenReturn(false);
        when(labelRepository.save(any(Label.class))).thenReturn(savedLabel);

        Label result = labelService.createLabel(label);

        assertNotNull(result);
        assertNotNull(result.getCreateDate());
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    void testUpdateLabel_Exists() {
        Label existingLabel = createTestLabel(1, "Old Name", LocalDateTime.now());
        Label updateDetails = new Label();
        updateDetails.setLabelName("New Name");

        when(labelRepository.findById(1)).thenReturn(Optional.of(existingLabel));
        when(labelRepository.existsByLabelName("New Name")).thenReturn(false);
        when(labelRepository.save(any(Label.class))).thenReturn(existingLabel);

        Label result = labelService.updateLabel(1, updateDetails);

        assertNotNull(result);
        verify(labelRepository, times(1)).findById(1);
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    void testUpdateLabel_NotExists() {
        Label updateDetails = new Label();
        updateDetails.setLabelName("New Name");

        when(labelRepository.findById(999)).thenReturn(Optional.empty());

        Label result = labelService.updateLabel(999, updateDetails);

        assertNull(result);
        verify(labelRepository, times(1)).findById(999);
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void testUpdateLabel_DuplicateName() {
        Label existingLabel = createTestLabel(1, "Old Name", LocalDateTime.now());
        Label updateDetails = new Label();
        updateDetails.setLabelName("Existing Name");

        when(labelRepository.findById(1)).thenReturn(Optional.of(existingLabel));
        when(labelRepository.existsByLabelName("Existing Name")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> labelService.updateLabel(1, updateDetails));
        verify(labelRepository, times(1)).findById(1);
        verify(labelRepository, times(1)).existsByLabelName("Existing Name");
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void testUpdateLabel_SameName() {
        Label existingLabel = createTestLabel(1, "Same Name", LocalDateTime.now());
        Label updateDetails = new Label();
        updateDetails.setLabelName("Same Name");

        when(labelRepository.findById(1)).thenReturn(Optional.of(existingLabel));
        when(labelRepository.save(any(Label.class))).thenReturn(existingLabel);

        Label result = labelService.updateLabel(1, updateDetails);

        assertNotNull(result);
        verify(labelRepository, times(1)).findById(1);
        verify(labelRepository, never()).existsByLabelName(anyString());
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    void testDeleteLabel_Exists() {
        when(labelRepository.existsById(1)).thenReturn(true);
        doNothing().when(labelRepository).deleteById(1);

        boolean result = labelService.deleteLabel(1);

        assertTrue(result);
        verify(labelRepository, times(1)).existsById(1);
        verify(labelRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteLabel_NotExists() {
        when(labelRepository.existsById(999)).thenReturn(false);

        boolean result = labelService.deleteLabel(999);

        assertFalse(result);
        verify(labelRepository, times(1)).existsById(999);
        verify(labelRepository, never()).deleteById(anyInt());
    }

    @Test
    void testSearchLabelsByName() {
        Label label1 = createTestLabel(1, "Programming Languages", LocalDateTime.now());
        Label label2 = createTestLabel(2, "Programming Basics", LocalDateTime.now());
        List<Label> mockLabels = Arrays.asList(label1, label2);

        when(labelRepository.findByLabelNameContaining("Programming")).thenReturn(mockLabels);

        List<Label> result = labelService.searchLabelsByName("Programming");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(labelRepository, times(1)).findByLabelNameContaining("Programming");
    }

    @Test
    void testSearchLabelsByName_EmptyResult() {
        when(labelRepository.findByLabelNameContaining("NonExistent")).thenReturn(Arrays.asList());

        List<Label> result = labelService.searchLabelsByName("NonExistent");

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(labelRepository, times(1)).findByLabelNameContaining("NonExistent");
    }

    @Test
    void testGetAllLabelsByCreateDate() {
        Label label1 = createTestLabel(1, "Programming", LocalDateTime.now().minusDays(1));
        Label label2 = createTestLabel(2, "Database", LocalDateTime.now());
        List<Label> mockLabels = Arrays.asList(label2, label1);

        when(labelRepository.findAllOrderByCreateDateDesc()).thenReturn(mockLabels);

        List<Label> result = labelService.getAllLabelsByCreateDate();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(labelRepository, times(1)).findAllOrderByCreateDateDesc();
    }

    @Test
    void testGetAllLabelsByName() {
        Label label1 = createTestLabel(1, "Database", LocalDateTime.now());
        Label label2 = createTestLabel(2, "Programming", LocalDateTime.now());
        List<Label> mockLabels = Arrays.asList(label1, label2);

        when(labelRepository.findAllOrderByLabelNameAsc()).thenReturn(mockLabels);

        List<Label> result = labelService.getAllLabelsByName();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(labelRepository, times(1)).findAllOrderByLabelNameAsc();
    }

    // Helper method to create test Label objects
    private Label createTestLabel(Integer id, String name, LocalDateTime createDate) {
        Label label = new Label();
        label.setLabelId(id);
        label.setLabelName(name);
        label.setCreateDate(createDate);
        return label;
    }
}

