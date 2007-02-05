class JournalsController < ApplicationController

  before_filter :game_creator_filter
 
  layout "main_frame"
  
  def show
    begin
      @player = Player.find(params[:player_id])
      @game = Game.find(params[:id])     
      @journal = Journal.find(@game.id,@player.playername)
    rescue
      @journal = Journal.empty_journal()
    end
  end
  
end