class MovesController < ApplicationController

  # GET moves.xml
  def index
    @moves = Move.find(:all)
  
    respond_to do |format|
      format.xml  { render :xml => @moves.to_xml }
    end
  end
  
end
