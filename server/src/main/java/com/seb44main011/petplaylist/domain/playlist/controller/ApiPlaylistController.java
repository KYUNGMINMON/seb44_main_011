package com.seb44main011.petplaylist.domain.playlist.controller;

import com.seb44main011.petplaylist.domain.member.entity.Member;
import com.seb44main011.petplaylist.domain.member.service.MemberService;
import com.seb44main011.petplaylist.domain.music.dto.MusicDto;
import com.seb44main011.petplaylist.domain.music.entity.Music;
import com.seb44main011.petplaylist.domain.music.mapper.MusicMapper;
import com.seb44main011.petplaylist.domain.music.service.MusicService;
import com.seb44main011.petplaylist.domain.playlist.dto.PlaylistDto;
import com.seb44main011.petplaylist.domain.playlist.entity.entityTable.PlayList;
import com.seb44main011.petplaylist.domain.playlist.mapper.MusicListMapper;
import com.seb44main011.petplaylist.domain.playlist.service.MusicListService;
import com.seb44main011.petplaylist.global.common.MultiResponseDto;
import com.seb44main011.petplaylist.global.stubData.StubData;
import com.seb44main011.petplaylist.global.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/playlist")
@Slf4j
@RequiredArgsConstructor
public class ApiPlaylistController {
    private final MusicMapper musicMapper;
    private final MusicListMapper musicListMapper;

    private final MemberService memberService;
    private final MusicListService musicListService;
    private final MusicService musicService;
    private final StubData stubData;
    @PostMapping("/test")
    public void postTest(){
        stubData.insertData();
    }

    @PostMapping(value = "/{member-id}", name = "music_name")
    public ResponseEntity<?> postPersonalPlayList(@PathVariable("member-id")@Positive long memberId,
                                                  @Valid @RequestBody MusicDto.PostRequest postRequest){
        Member member = memberService.findMember(memberId);
        Music music = musicService.findMusic(postRequest.getMusicId());

        PlayList newPlayList = musicListMapper.memberAndMusicToMusicList(member,music);

        musicListService.addMusicList(newPlayList);
        URI location = UriCreator.createUri("/api/playlist");
        return ResponseEntity.created(location).build();
    }


    @GetMapping(value = "/{member-id}", params = {"page"})
    public ResponseEntity<?> getPersonalPlayList(@PathVariable("member-id")@Positive long memberId,
                                                  @Valid @RequestParam(name = "page", defaultValue = "1") @Positive int page){
        Page<PlayList> musicListList = musicListService.findPersonalMusicListsPage(memberId,page);
        List<PlaylistDto.ApiResponse> responseMusic = musicListMapper.musicListToPlayListResponseList(musicListList.getContent());
        return new ResponseEntity<>(
                new MultiResponseDto<>(responseMusic,musicListList), HttpStatus.OK);

    }

    @GetMapping(value = "/{dogOrCats}/id/{memberId}",params = {"page"})
    public ResponseEntity<?> getPersonalPlayListByCategoryAndTags(@PathVariable(name = "dogOrCats") String dogOrCats,@PathVariable(name = "memberId") long memberId,
                                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                                        @RequestParam(name = "tags",required = false)String tags){

        Music.Category category = Music.Category.valueOf(dogOrCats.toUpperCase());
        Page<Music> musicPage = musicService.findCategoryAndTagsPageMusic(category,tags,page);
        List<Music> musicList = musicPage.getContent();
        List<PlayList> likeMusic = memberService.findMember(memberId).getPlayLists();
        List<PlaylistDto.ApiResponse> apiResponse = musicListMapper.musicListToCategoryPlayListApiResponse(musicList,likeMusic);

        return new ResponseEntity<>(
                new MultiResponseDto<>(apiResponse,musicPage), HttpStatus.OK);

    }

    @DeleteMapping(value = "/{member-id}", name = "music_name")
    public ResponseEntity<?> deletePersonalPlayList(@PathVariable("member-id")@Positive long id,
                                                  @Valid @RequestBody MusicDto.DeleteRequest postRequest){
        Member member = memberService.findMember(id);
        Music music = musicService.findMusic(postRequest.getMusicId());
        musicListService.deleteMusicList(member, music);
        return ResponseEntity.noContent().build();
    }


}
