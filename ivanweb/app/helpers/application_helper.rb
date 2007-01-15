# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper

  def admin
    ( session['user'] and session['user'].admin )
  end

end
