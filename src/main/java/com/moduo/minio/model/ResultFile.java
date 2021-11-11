package com.moduo.minio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Wu Zicong
 * @create 2021-11-10 16:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultFile {
    private String link;
    private String name;
    private String originalName;
}
