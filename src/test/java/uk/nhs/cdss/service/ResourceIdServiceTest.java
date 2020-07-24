package uk.nhs.cdss.service;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.entities.ResourceId;
import uk.nhs.cdss.repos.ResourceIdRepository;

@RunWith(MockitoJUnitRunner.class)
public class ResourceIdServiceTest {

  private ResourceIdService resourceIdService;

  @Mock
  private ResourceIdRepository idRepository;

  @Before
  public void before() {
    resourceIdService = new ResourceIdService(idRepository);
  }

  @Test
  public void createsNewId() {

    when(idRepository.existsById(ResourceId.GLOBAL)).thenReturn(false);

    ResourceId expectedInitialId = new ResourceId(ResourceId.GLOBAL);
    when(idRepository.save(expectedInitialId)).thenReturn(expectedInitialId);

    Long returnedId = resourceIdService.nextId();

    assertThat(returnedId, is(ResourceId.INITIAL_VALUE));
  }

  @Test
  public void incrementsId() {
    when(idRepository.existsById(1L)).thenReturn(true);
    when(idRepository.incrementAndGet(1L)).thenReturn(2L);

    Long returnedId = resourceIdService.nextId();

    assertThat(returnedId, is(2L));
  }

}