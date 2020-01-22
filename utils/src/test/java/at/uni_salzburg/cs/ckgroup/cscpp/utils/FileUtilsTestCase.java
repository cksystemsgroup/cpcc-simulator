package at.uni_salzburg.cs.ckgroup.cscpp.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTestCase
{

    @Test
    public void testDefaultConstructor()
    {
        FileUtils fu = new FileUtils();
        Assert.assertNotNull(fu);
    }

    @Test
    public void testEnsureDirectory() throws IOException
    {

        File workDir = File.createTempFile("edtest", "dir");
        workDir.delete();
        Assert.assertFalse(workDir.exists());

        File subDir = new File(workDir, "subdir");
        Assert.assertFalse(subDir.exists());

        FileUtils.ensureDirectory(workDir);
        Assert.assertTrue(workDir.exists());

        workDir.setWritable(false, false);

        try
        {
            FileUtils.ensureDirectory(subDir);
        }
        catch (IOException e)
        {
            Assert.assertEquals("Can not create directory " + subDir.getAbsolutePath(), e.getMessage());
        }

        workDir.setWritable(true);
        workDir.delete();
        Assert.assertFalse(workDir.exists());
    }

    @Test
    public void testZipToStream() throws IOException
    {

        URL url = FileUtilsTestCase.class.getResource("zip");
        File f = new File(url.getFile());
        int prefixLength = f.getAbsolutePath().length() + 1;
        Map<String, ZipEntry> filesMap = new HashMap<String, ZipEntry>();
        traverseDir(f, filesMap, prefixLength);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(bo);

        FileUtils.zipToStream(f, f.getAbsolutePath().length() + 1, out);
        out.close();

        Assert.assertFalse(bo.size() == 0);

        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ZipInputStream zip = new ZipInputStream(bi);

        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null)
        {
            String name = entry.getName();
            long size = entry.getSize();
            long time = entry.getTime();

            ZipEntry expectedEntry = filesMap.get(name);
            Assert.assertNotNull(name, expectedEntry);
            // Assert.assertEquals("Time of entry " + name, expectedEntry.getTime(), time);
            Assert.assertTrue("Time of entry " + name, Math.abs(expectedEntry.getTime() - time) < 1001);

            if (entry.isDirectory())
            {
                System.out.println("Deflating folder " + name + ", time=" + time);
            }
            else
            {
                System.out.print("Deflating file   " + name + ", size=" + size + ", time=" + time);
                ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
                int l;
                byte[] tmp = new byte[2048];
                while ((l = zip.read(tmp)) != -1)
                {
                    bo2.write(tmp, 0, l);
                }
                System.out.println(", bytes read=" + bo2.size());
                Assert.assertEquals("Size of entry " + name, expectedEntry.getSize(), bo2.size());

                int prfx = name.lastIndexOf(File.separatorChar) + 1;
                String expectedContent = name.substring(prfx).toUpperCase();
                Assert.assertEquals(expectedContent, bo2.toString());
            }
        }
    }

    private void traverseDir(File file, Map<String, ZipEntry> filesMap, int prefixLength)
    {
        String name;
        if (file.getAbsolutePath().length() < prefixLength)
            name = "";
        else
            name = file.getAbsolutePath().substring(prefixLength) + (file.isDirectory() ? "/" : "");

        if (!name.isEmpty())
        {
            ZipEntry e = new ZipEntry(name);
            e.setSize(file.length());
            e.setTime(file.lastModified());
            filesMap.put(name, e);
        }

        if (file.isDirectory())
            for (String f : file.list())
                traverseDir(new File(file, f), filesMap, prefixLength);
    }

}
