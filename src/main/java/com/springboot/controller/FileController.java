package com.springboot.controller;

import com.springboot.entity.User;
import com.springboot.entity.UserFile;
import com.springboot.service.UserFileService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("file")
public class FileController {

    @Autowired
    private UserFileService userFileService;

    @GetMapping("delete")
    public String delete(Integer id) throws FileNotFoundException {
        UserFile userFile = userFileService.findById(id);
        String realPath = ResourceUtils.getURL("classpath:").getPath() + "/static" + userFile.getPath();
        File file = new File(realPath,userFile.getNewFileName());
        if(file.exists())file.delete();
        userFileService.delete(id);
        return "redirect:/file/showAll";
    }

    @GetMapping("download")
    public void download(Integer id,
                         String openStyle,
                         HttpServletResponse response) throws IOException {
        UserFile userFile = userFileService.findById(id);
        if("attachment".equals(openStyle)){
            userFile.setDowncounts(userFile.getDowncounts() + 1);
            userFileService.update(userFile);
        }
        String realPath = ResourceUtils.getURL("classpath:").getPath() + "/static" + userFile.getPath();
        FileInputStream is = new FileInputStream(new File(realPath, userFile.getNewFileName()));
        response.setHeader("content-disposition", openStyle + ";fileName=" + URLEncoder.encode(userFile.getOldFileName(), "UTF-8"));
        ServletOutputStream os = response.getOutputStream();
        IOUtils.copy(is, os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
    }

    @PostMapping("upload")
    public String upload(MultipartFile uploadFile,
                         HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");

        String oldFileName = uploadFile.getOriginalFilename();
        String extension = "." + FilenameUtils.getExtension(uploadFile.getOriginalFilename());
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                UUID.randomUUID().toString().replace("-", "") +
                extension;
        Long size = uploadFile.getSize();
        String type = uploadFile.getContentType();

        //
        String realPath = ResourceUtils.getURL("classpath:").getPath() + "/static/files";

        String dataFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dateDirPath = realPath + "/" + dataFormat;
        File dateDir = new File(dateDirPath);
        if(!dateDir.exists()){
            dateDir.mkdirs();
        }

        // upload file
        uploadFile.transferTo(new File(dateDir, newFileName));

        // put file in database
        UserFile userFile = new UserFile();
        userFile.setOldFileName(oldFileName)
                .setNewFileName(newFileName)
                .setExt(extension)
                .setSize(String.valueOf(size))
                .setType(type)
                .setPath("/files/" + dataFormat)
                .setUserId(user.getId());
        userFileService.save(userFile);
        return "redirect:/file/showAll";
    }

    @GetMapping("showAll")
    public String findAll(HttpSession session,
                          Model model){
        User user =(User) session.getAttribute("user");
        List<UserFile> userFiles = userFileService.findByUserId(user.getId());
        model.addAttribute("files",userFiles);
        return "showAll";
    }
}
