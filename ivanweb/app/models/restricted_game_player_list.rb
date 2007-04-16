
class RestrictedGamePlayerList < ActiveRecord::Base
  set_table_name "player_game"   
  
  def self.get_players( game_id )
    game_player_list = find( :all, :conditions => ["fk_game_id = ?", game_id ])
    players = []
    for game_player in game_player_list
      begin
        players << Player.find(game_player.fk_player_id)
      rescue
      end
    end
    players
  end
   
end
