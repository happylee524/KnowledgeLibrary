package com.lib.service.user.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.lib.dao.FileInfoDao;
import com.lib.entity.FileInfo;
import com.lib.entity.UserInfo;
import com.lib.enums.Const;
import com.lib.service.user.FileInfoService;
import com.lib.service.user.OfficeConvert;
import com.lib.utils.CompressUtil;
import com.lib.utils.JudgeUtils;
import com.lib.utils.TranslateUtils;

@Service
public class FileInfoServiceImpl implements FileInfoService {
	private OfficeConvert officeConvert = TranslateUtils.getOfficeConvert();
	@Autowired
	private FileInfoDao fileinfoDao;
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Override
	public int insertFile(FileInfo fileInfo) {

		return fileinfoDao.insert(fileInfo);
	}

	@Override
	public List<String> compressFile(String name, UserInfo user) throws Exception {
		List<String> uuids = new ArrayList<String>();
		List<FileInfo> files = CompressUtil.startCompress(name, user.getUserId());
		try {
			FileUtils.forceDelete(new File(name));
		} catch (Exception e) {
			LOG.error("删除文件失败" + name);
		}
		for (FileInfo f : files) {
			f.setFileUserId(user.getUserId());
			f.setFileClassId(1l);
			f.setFileBrief("无");
			fileinfoDao.insert(f);
			uuids.add(f.getFileUuid());
		}
		return uuids;
	}

	@Override
	public List<FileInfo> StartTransfor() {
		new Thread() {
			public void run() {
				PageHelper.startPage(1, 5, "file_id asc");
				List<FileInfo> lists = fileinfoDao.getFilesByState(2);
				for (FileInfo l : lists) {
					System.out.println(l);
				}
			};
		}.start();

		List<FileInfo> files = fileinfoDao.getFilesByState(2);
		return files;
	}

	@Override
	public void translateFile(String uuid) {

		FileInfo file = fileinfoDao.getFileInfoByUuid(uuid);
		LOG.debug("开始转化文件" + uuid);
		if (JudgeUtils.isOfficeFile(file.getFileExt())) {
			// 文档转化
			officeConvert.convertToPDF(new File(Const.ROOT_PATH + file.getFilePath() + "." + file.getFileExt()),
					new File(Const.ROOT_PATH + file.getFilePath() + ".pdf"));
			// 获取pdf缩略图  路径为 + Const.ROOT_PATH + file.getFilePath()+".png"
			// docService.pdfGetThumb(Const.Root_PATH + "upload/doc/" + uuid +
			// ".pdf", uuid);
		}
		
		//全文检索创立索引
		
		
		
	}

}
