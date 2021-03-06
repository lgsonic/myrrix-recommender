/*
 * Copyright Myrrix Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.myrrix.web.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mahout.cf.taste.common.TasteException;

import net.myrrix.common.MyrrixRecommender;

/**
 * <p>Responds to a POST request to {@code /tag/user/[userID]/[tag]} and in turn calls
 * {@link MyrrixRecommender#setUserTag(long, String, float)}. If the request body is empty,
 * the value is 1.0, otherwise the value in the request body's first line is used.</p>
 *
 * @author Sean Owen
 * @since 1.0
 */
public final class TagUserServlet extends AbstractMyrrixServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No path");      
    }
    Iterator<String> pathComponents = SLASH.split(pathInfo).iterator();
    long userID;
    String userTag;
    try {
      userID = Long.parseLong(pathComponents.next());
      userTag = pathComponents.next();
    } catch (NoSuchElementException nsee) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, nsee.toString());
      return;
    } catch (NumberFormatException nfe) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, nfe.toString());
      return;
    }
    if (pathComponents.hasNext()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Path too long");
      return;
    }

    float tagValue;
    try {
      tagValue = PreferenceServlet.readValue(request);
    } catch (IllegalArgumentException ignored) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad value");
      return;
    }

    MyrrixRecommender recommender = getRecommender();
    try {
      recommender.setUserTag(userID, userTag, tagValue);
    } catch (TasteException te) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, te.toString());
      getServletContext().log("Unexpected error in " + getClass().getSimpleName(), te);
    }
  }

  @Override
  protected Long getUnnormalizedPartitionToServe(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      return null;
    }
    Iterator<String> pathComponents = SLASH.split(pathInfo).iterator();
    long userID;
    try {
      userID = Long.parseLong(pathComponents.next());
    } catch (NoSuchElementException ignored) {
      return null;
    } catch (NumberFormatException ignored) {
      return null;
    }
    return userID;
  }

}
