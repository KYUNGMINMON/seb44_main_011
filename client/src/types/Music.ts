export interface Music {
  musicId: number;
  title: string;
  music_url: string;
  image_url: string;
  category: string;
  tags: string;
  likes?: boolean;
}
