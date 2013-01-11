package com.adobe.epubcheck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class Archive {

	ArrayList<String> paths;

	ArrayList<String> names;

	File baseDir;

	File epubFile;

	String epubName;

	boolean deleteOnExit = true;

	public Archive(String base, boolean save) {
		this.deleteOnExit = !save;
		baseDir = new File(base);
		if (!baseDir.exists() || !baseDir.isDirectory())
			throw new RuntimeException(
					"The path specified for the archive is invalid");
		epubName = baseDir.getName() + ".epub";
		epubFile = new File(epubName);
		if (deleteOnExit)
			epubFile.deleteOnExit();

		paths = new ArrayList<String>();
		names = new ArrayList<String>();
	}

	public Archive(String base) {
		this(base, false);
	}

	public String getEpubName() {
		return epubName;
	}

	public File getEpubFile() {
		return epubFile;
	}

	public void deleteEpubFile() {
		epubFile.delete();
	}

	public void createArchive() {
		// using commons compress to allow setting filename encoding pre java7
		ZipArchiveOutputStream out = null;
		try {

			collectFiles(baseDir, "");
						
			//make mimetype the first entry
			int mimetype = names.indexOf("mimetype");
			if(mimetype > -1 ) {
				String name = names.remove(mimetype);
				String path = paths.remove(mimetype);
				names.add(0, name);
				paths.add(0, path);				
			} else {
				System.err.println("No mimetype file found in expanded publication, output archive will be invalid");
			}
			
						
			out = new ZipArchiveOutputStream(epubFile);
			out.setEncoding("UTF-8");

			for (int i = 0; i < paths.size(); i++) {
				ZipArchiveEntry entry = new ZipArchiveEntry(new File(paths.get(i)), names.get(i));
				if(i == 0) {
					entry.setMethod(ZipArchiveEntry.STORED);
					entry.setSize(getSize(paths.get(i)));
					entry.setCrc(getCRC(paths.get(i)));
				} else {
					entry.setMethod(ZipArchiveEntry.DEFLATED);
				}
				out.putArchiveEntry(entry);
				FileInputStream in = new FileInputStream(paths.get(i));
				byte[] buf = new byte[1024];
				int len = 0;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.closeArchiveEntry();				
			}			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally{
			try {
				out.flush();
				out.finish();
				out.close();
			} catch (IOException e) {
			}
			
		}

	}

	private long getSize(String path) throws IOException {
		FileInputStream in = null;
		try{
			in = new FileInputStream(path);
			byte[] buf = new byte[1024];
			int len = 0;
			int size = 0;
			while ((len = in.read(buf)) > 0) {
				size += len;
			}		
			return size;
		}finally{
			in.close();
		}
	}

	private long getCRC(String path) throws IOException {
		CheckedInputStream cis = null;
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(path);
			cis = new CheckedInputStream(fis, new CRC32());
			byte[] buf = new byte[128];
			while (cis.read(buf) >= 0) {}	
		} finally {
			fis.close();
			cis.close();
		}
		return cis.getChecksum().getValue();
	}

//	public void createArchiveOld() {
//		collectFiles(baseDir, "");
//		byte[] buf = new byte[1024];
//		try {
//
//			ZipOutputStream out = new ZipOutputStream((new FileOutputStream(
//					epubName)));
//
//			int index = names.indexOf("mimetype");
//			if (index >= 0) {
//				FileInputStream in = new FileInputStream(paths.get(index));
//
//				ZipEntry entry = new ZipEntry(names.get(index));
//				entry.setMethod(ZipEntry.STORED);
//				int len, size = 0;
//				while ((len = in.read(buf)) > 0)
//					size += len;
//
//				in = new FileInputStream(paths.get(index));
//
//				entry.setCompressedSize(size);
//				entry.setSize(size);
//
//				CRC32 crc = new CRC32();
//				entry.setCrc(crc.getValue());
//				out.putNextEntry(entry);
//
//				while ((len = in.read(buf)) > 0) {
//					crc.update(buf, 0, len);
//					out.write(buf, 0, len);
//				}
//
//				entry.setCrc(crc.getValue());
//
//				paths.remove(index);
//				names.remove(index);
//			}
//
//			for (int i = 0; i < paths.size(); i++) {
//				FileInputStream in = new FileInputStream(paths.get(i));
//
//				out.putNextEntry(new ZipEntry(names.get(i)));
//
//				int len;
//				while ((len = in.read(buf)) > 0) {
//					out.write(buf, 0, len);
//				}
//
//				out.closeEntry();
//				in.close();
//			}
//
//			out.close();
//		} catch (IOException e) {
//		}
//	}

	private void collectFiles(File dir, String dirName) {
		if (!dirName.equals("") && !dirName.endsWith("/")) {
			dirName = dirName + "/";
		}

		File files[] = dir.listFiles();

		for (int i = 0; i < files.length; i++)
			if (files[i].isFile()) {
				names.add(dirName + files[i].getName());
				paths.add(files[i].getAbsolutePath());
			} else if (!files[i].getName().equals(".svn"))
				collectFiles(files[i], dirName + files[i].getName() + "/");
	}

	public void listFiles() {
		for (int i = 0; i < names.size(); i++)
			System.out.println(names.get(i));
	}
}
