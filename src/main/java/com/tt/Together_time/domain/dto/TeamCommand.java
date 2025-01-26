package com.tt.Together_time.domain.dto;

import com.tt.Together_time.domain.rdb.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamCommand {
    private Member member;
    private Long projectId;
}
