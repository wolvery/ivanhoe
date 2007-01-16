class JournalsController < ApplicationController

  before_filter :game_creator_filter
 
  layout "main_frame"
  
  def show
    @journal = Journal.find(params[:id],params[:player])
    @playername = params[:player] 
  end
  
end