package com.adobe.epubcheck.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.adobe.epubcheck.util.FeatureEnum;

public class FeatureReportTest
{

  @Test
  public void snapshotIsDetachedFromLaterWrites()
  {
    FeatureReport publication = new FeatureReport();
    publication.report(FeatureEnum.DC_TITLE, location("a.xhtml"), "A");

    FeatureReport snapshot = publication.snapshot();
    publication.report(FeatureEnum.DC_TITLE, location("b.xhtml"), "B");

    FeatureReport worker = snapshot.fork();
    assertEquals("the snapshot must not see the later feature", 1,
        worker.getFeature(FeatureEnum.DC_TITLE).size());
    assertFalse(worker.hasFeature(FeatureEnum.HAS_SCRIPTS));

    worker.report(FeatureEnum.HAS_SCRIPTS, location("a.xhtml"));
    publication.mergeSnapshot(worker);

    assertEquals("the snapshot features must not be merged twice", 2,
        publication.getFeature(FeatureEnum.DC_TITLE).size());
    assertTrue(publication.hasFeature(FeatureEnum.HAS_SCRIPTS));
  }

  @Test
  public void snapshotOfForkIncludesInheritedFeatures()
  {
    FeatureReport publication = new FeatureReport();
    publication.report(FeatureEnum.DC_TITLE, location("root.xhtml"), "root");

    FeatureReport fork = publication.fork();
    fork.report(FeatureEnum.DC_CREATOR, location("fork.xhtml"), "creator");

    FeatureReport snapshot = fork.snapshot();
    publication.report(FeatureEnum.DC_TITLE, location("later.xhtml"), "later");
    fork.report(FeatureEnum.DC_CONTRIBUTOR, location("later.xhtml"), "later");

    assertEquals("the root feature must be inherited", 1,
        snapshot.getFeature(FeatureEnum.DC_TITLE).size());
    assertEquals("root",
        snapshot.getFeature(FeatureEnum.DC_TITLE).iterator().next().getValue().get());
    assertEquals("the fork feature must be included", 1,
        snapshot.getFeature(FeatureEnum.DC_CREATOR).size());
    assertFalse("writes made after the snapshot must not be visible",
        snapshot.hasFeature(FeatureEnum.DC_CONTRIBUTOR));
  }

  @Test
  public void snapshotPreservesFeatureOrder()
  {
    FeatureReport publication = new FeatureReport();
    for (String path : Arrays.asList("ch1.xhtml", "ch2.xhtml", "ch3.xhtml"))
    {
      publication.report(FeatureEnum.SECTIONS, location(path), path);
    }

    FeatureReport snapshot = publication.snapshot();
    List<String> values = new ArrayList<>();
    for (FeatureReport.Feature feature : snapshot.getFeature(FeatureEnum.SECTIONS))
    {
      values.add(feature.getValue().get());
    }
    assertEquals(Arrays.asList("ch1.xhtml", "ch2.xhtml", "ch3.xhtml"), values);
  }

  @Test(expected = IllegalStateException.class)
  public void snapshotRejectsWrites()
  {
    FeatureReport snapshot = new FeatureReport().snapshot();
    snapshot.report(FeatureEnum.HAS_SCRIPTS, location("ch1.xhtml"));
  }

  @Test
  public void mergePreservesForkOrder()
  {
    FeatureReport publication = new FeatureReport();
    FeatureReport snapshot = publication.snapshot();

    List<FeatureReport> workers = new ArrayList<>();
    for (String path : Arrays.asList("ch1.xhtml", "ch2.xhtml", "ch3.xhtml"))
    {
      FeatureReport worker = snapshot.fork();
      worker.report(FeatureEnum.SECTIONS, location(path), path);
      workers.add(worker);
    }

    for (FeatureReport worker : workers)
    {
      publication.mergeSnapshot(worker);
    }

    List<String> values = new ArrayList<>();
    for (FeatureReport.Feature feature : publication.getFeature(FeatureEnum.SECTIONS))
    {
      values.add(feature.getValue().get());
    }
    assertEquals(Arrays.asList("ch1.xhtml", "ch2.xhtml", "ch3.xhtml"), values);
  }

  @Test
  public void laterForkDoesNotObserveEarlierMerges()
  {
    FeatureReport publication = new FeatureReport();
    FeatureReport snapshot = publication.snapshot();

    FeatureReport first = snapshot.fork();
    first.report(FeatureEnum.HAS_MICRODATA, location("ch1.xhtml"));
    publication.mergeSnapshot(first);

    FeatureReport later = snapshot.fork();
    assertFalse("a later worker must not observe the earlier merge",
        later.hasFeature(FeatureEnum.HAS_MICRODATA));

    later.report(FeatureEnum.HAS_MICRODATA, location("ch2.xhtml"));
    publication.mergeSnapshot(later);

    assertEquals("both resources must keep their own feature", 2,
        publication.getFeature(FeatureEnum.HAS_MICRODATA).size());
  }

  @Test
  public void snapshotCanBeReadWhileResultsAreMerged() throws Exception
  {
    final FeatureReport publication = new FeatureReport();
    publication.report(FeatureEnum.DC_TITLE, location("root.xhtml"), "root");
    final FeatureReport snapshot = publication.snapshot();

    final int readers = 4;
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch reading = new CountDownLatch(readers);
    final CountDownLatch done = new CountDownLatch(readers);
    final AtomicReference<Throwable> failure = new AtomicReference<>();
    ExecutorService executor = Executors.newFixedThreadPool(readers);
    try
    {
      for (int i = 0; i < readers; i++)
      {
        executor.execute(() ->
        {
          FeatureReport worker = snapshot.fork();
          try
          {
            start.await();
            reading.countDown();
            for (int j = 0; j < 1000; j++)
            {
              assertEquals(1, worker.getFeature(FeatureEnum.DC_TITLE).size());
              assertFalse(worker.hasFeature(FeatureEnum.HAS_SCRIPTS));
            }
          } catch (Throwable t)
          {
            failure.compareAndSet(null, t);
          } finally
          {
            done.countDown();
          }
        });
      }

      start.countDown();
      assertTrue("workers must be reading before merging starts",
          reading.await(10, TimeUnit.SECONDS));
      for (int i = 0; i < 500; i++)
      {
        FeatureReport worker = snapshot.fork();
        worker.report(FeatureEnum.HAS_SCRIPTS, location("ch" + i + ".xhtml"));
        publication.mergeSnapshot(worker);
      }
      assertTrue("workers must finish reading", done.await(10, TimeUnit.SECONDS));
    } finally
    {
      executor.shutdownNow();
    }
    if (failure.get() != null)
    {
      throw new AssertionError(failure.get());
    }
  }

  private static EPUBLocation location(String path)
  {
    return EPUBLocation.of(new File(path));
  }
}
